package com.pokevault.modules.web.controller;

import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.web.dto.view.AccountsPage;
import com.pokevault.modules.web.dto.view.CardGalleryPage;
import com.pokevault.modules.web.dto.view.DashboardPage;
import com.pokevault.modules.web.dto.view.InventoryPage;
import com.pokevault.modules.web.dto.view.OrderView;
import com.pokevault.modules.web.service.WebPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * Serves the Thymeleaf pages under templates/. Presentation layer only: reads request parameters,
 * asks {@link WebPageService} for the page data and puts it in the Model under the names the templates use.
 */
@Controller
@RequiredArgsConstructor
public class WebViewController {

    private final WebPageService webPageService;

    /** Id of the signed-in user for the customer order form; null for guests. */
    @ModelAttribute("currentUserId")
    public Long currentUserId(Principal principal) {
        return principal == null ? null : webPageService.findUserId(principal.getName()).orElse(null);
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        DashboardPage page = webPageService.getDashboard();
        model.addAttribute("totalCards", page.getTotalCards());
        model.addAttribute("totalExpansions", page.getTotalExpansions());
        model.addAttribute("totalOrders", page.getTotalOrders());
        model.addAttribute("featuredCards", page.getFeaturedCards());
        return "dashboard";
    }

    @GetMapping("/cards")
    public String cards(@RequestParam(required = false) String element,
                        @RequestParam(required = false) String rarity,
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) String search,
                        Model model) {
        CardGalleryPage page = webPageService.getCardGallery(element, rarity, type, search);
        model.addAttribute("cards", page.getCards());
        model.addAttribute("totalElements", page.getCards().size());
        model.addAttribute("selectedElement", page.getSelectedElement());
        model.addAttribute("selectedRarity", page.getSelectedRarity());
        model.addAttribute("selectedType", page.getSelectedType());
        model.addAttribute("search", page.getSearch());
        model.addAttribute("allElementsUrl", page.getAllElementsUrl());
        model.addAttribute("elementFilters", page.getElementFilters());
        model.addAttribute("allRaritiesUrl", page.getAllRaritiesUrl());
        model.addAttribute("rarityFilters", page.getRarityFilters());
        model.addAttribute("allTypesUrl", page.getAllTypesUrl());
        model.addAttribute("typeFilters", page.getTypeFilters());
        return "cards";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        InventoryPage page = webPageService.getInventoryPage();
        model.addAttribute("inventories", page.getInventories());
        model.addAttribute("totalElements", page.getInventories().size());
        model.addAttribute("customers", page.getCustomers());
        return "inventory";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        AccountsPage page = webPageService.getAccountsPage();
        model.addAttribute("accounts", page.getAccounts());
        model.addAttribute("totalAccounts", page.getAccounts().size());
        model.addAttribute("readyAccounts", page.getReadyAccounts());
        model.addAttribute("cooldownAccounts", page.getCooldownAccounts());
        model.addAttribute("totalElements", page.getAccounts().size());
        model.addAttribute("customers", page.getCustomers());
        model.addAttribute("membershipTiers", MembershipTier.values());
        return "accounts";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", webPageService.getAllOrders());
        return "orders";
    }

    /** Read-only order history of the signed-in user; /orders stays the staff console for every order. */
    @GetMapping("/my-orders")
    public String myOrders(Principal principal, Model model) {
        List<OrderView> orders = webPageService.getOrdersOfUser(principal.getName());
        model.addAttribute("orders", orders);
        return "my-orders";
    }
}
