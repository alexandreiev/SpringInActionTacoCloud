package tacos.data;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tacos.model.Ingredient;
import tacos.model.Taco;

import java.sql.*;
import java.util.Arrays;
import java.util.Date;

@Repository
public class JdbcTacoRepository implements TacoRepository {
    
    private JdbcTemplate jdbc;
    
//    @Autowired
    public JdbcTacoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    
    @Override
    public Taco save(Taco taco) {
        long tacoId = saveTacoInfo(taco);
        taco.setId(tacoId);
        for (var ingredient : taco.getIngredients()) {
            saveIngredientToTaco(ingredient, tacoId);
        }
        return taco;
    }
    
    private long saveTacoInfo(Taco taco) {
        taco.setCreatedAt(new Date());
        var preparedFactory =  new PreparedStatementCreatorFactory(
                "insert into taco (name, created_at) values (?, ?)",
                Types.VARCHAR, Types.TIMESTAMP);
        preparedFactory.setReturnGeneratedKeys(true);
        PreparedStatementCreator psc = preparedFactory
                .newPreparedStatementCreator(
                        Arrays.asList(
                                taco.getName(),
                                new Timestamp(taco.getCreatedAt().getTime()))
                );
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(psc, keyHolder);
        return keyHolder.getKey().longValue();
    }
    
    private void saveIngredientToTaco(Ingredient ingredient, long tacoId) {
        jdbc.update("insert into taco_ingredients (taco_id, ingredient_id) " +
                "values (?, ?)", tacoId, ingredient.getId());
    }
}
