package tacos.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import tacos.*;
import tacos.Ingredient.Type;
import tacos.data.IngredientJPARepository;
import tacos.data.IngredientRepository;
import tacos.data.TacoJPARepository;
import tacos.data.TacoRepository;
import tacos.security.UserRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/design")
@SessionAttributes("order")
public class DesignTacoController {

//    private final IngredientRepository ingredientRepo;
//    private final TacoRepository designRepo;
    private final IngredientJPARepository ingredientRepo;
    private final TacoJPARepository designRepo;
    private UserRepository userRepo;

    @Autowired
//    public DesignTacoController(IngredientRepository ingredientRepo,
//                                TacoRepository designRepo) {
    public DesignTacoController(IngredientJPARepository ingredientRepo,
                                TacoJPARepository designRepo, UserRepository userRepo) {
        this.ingredientRepo = ingredientRepo;
        this.designRepo = designRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String showDesignForm(Model model, Principal principal){
        var ingredients = new ArrayList<Ingredient>();
        ingredientRepo.findAll().forEach(i -> ingredients.add(i));
        var types = Ingredient.Type.values();
        for (var type: types) {
            model.addAttribute(type.toString().toLowerCase(), filterByType(ingredients, type));
        }

        String username = principal.getName();
        var user = userRepo.findByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("design", new Taco());
        return "design";
    }

    @ModelAttribute(name = "order")
    public Order order() {
        return new Order();
    }

    @ModelAttribute(name = "taco")
    public Taco taco() {
        return new Taco();
    }

    @PostMapping
    public String processDesign(@Valid Taco design, @ModelAttribute Order order, Errors errors) {
        if (errors.hasErrors()) {
            return "design";
        }

        Taco saved = designRepo.save(design);
        order.addDesign(saved);
        log.info("Processing design: " + design);
        return "redirect:/orders/current";
    }

    private List<Ingredient> filterByType(List<Ingredient> ingredients, Type type)
    {
        return ingredients.stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }
}
