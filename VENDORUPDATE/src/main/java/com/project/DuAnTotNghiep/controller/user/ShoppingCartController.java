package com.project.DuAnTotNghiep.controller.user;

import com.project.DuAnTotNghiep.dto.AddressShipping.AddressShippingDto;
import com.project.DuAnTotNghiep.dto.Cart.CartDto;
import com.project.DuAnTotNghiep.dto.Cart.GuestCartDto;
import com.project.DuAnTotNghiep.dto.Cart.ProductCart;
import com.project.DuAnTotNghiep.dto.DiscountCode.DiscountCodeDto;
import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.repository.ProductRepository;
import com.project.DuAnTotNghiep.service.AddressShippingService;
import com.project.DuAnTotNghiep.service.BillService;
import com.project.DuAnTotNghiep.service.CartService;
import com.project.DuAnTotNghiep.service.DiscountCodeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

@Controller
public class ShoppingCartController {
	private final CartService cartService;
	private final BillService billService;
	private final DiscountCodeService discountCodeService;
	private final AddressShippingService addressShippingService;
	private final ProductRepository productRepository;

	public ShoppingCartController(CartService cartService, BillService billService,
			DiscountCodeService discountCodeService, AddressShippingService addressShippingService,
			ProductRepository productRepository) {
		this.cartService = cartService;
		this.billService = billService;
		this.discountCodeService = discountCodeService;
		this.addressShippingService = addressShippingService;
		this.productRepository = productRepository;
	}

	@GetMapping("/shoping-cart")
    public String viewShoppingCart(Model model, HttpSession session) {
        List<CartDto> cartDtoList = new ArrayList<>();
        boolean isGuest = true;

        // ✅ Nếu người dùng đăng nhập -> lấy giỏ hàng từ DB
        try {
            List<CartDto> userCart = cartService.getAllCartByAccountId();
            if (userCart != null && !userCart.isEmpty()) {
                cartDtoList = userCart;
                isGuest = false;
            }
        } catch (Exception ignored) {
            // User chưa đăng nhập hoặc không có giỏ hàng trong DB
        }

        // ✅ Nếu Guest -> lấy giỏ hàng từ session
        if (isGuest) {
            List<GuestCartDto> guestCart = (List<GuestCartDto>) session.getAttribute("guestCart");
            
            if (guestCart != null && !guestCart.isEmpty()) {
                for (GuestCartDto g : guestCart) {
                    CartDto c = new CartDto();
                    
                    // ✅ Set ID giả để JavaScript có thể hoạt động
                    c.setId(g.getProductId());  // Dùng productId làm cartId tạm
                    c.setQuantity(g.getQuantity());

                    // ✅ Map thông tin sản phẩm
                    ProductCart p = new ProductCart();
                    p.setProductId(g.getProductId());  // ✅ Sửa từ setId() → setProductId()
                    p.setName(g.getName());
                    p.setImageUrl(g.getImageUrl());
                    p.setPrice(g.getPrice());

                    c.setProduct(p);
                    
                    // ✅ QUAN TRỌNG: Set detail = null để HTML biết là guest cart
                    c.setDetail(null);

                    cartDtoList.add(c);
                }
            }
        }

		// ✅ Mã giảm giá và địa chỉ (nếu có)
		Page<DiscountCodeDto> discountCodeList = Page.empty();
		try {
			discountCodeList = discountCodeService.getAllAvailableDiscountCode(PageRequest.of(0, 15));
		} catch (Exception ignored) {
		}

		List<AddressShippingDto> addressShippingDtos = new ArrayList<>();
		try {
			addressShippingDtos = addressShippingService.getAddressShippingByAccountId();
		} catch (Exception ignored) {
		}

		// ✅ Gửi dữ liệu ra view
		model.addAttribute("discountCodes", discountCodeList.getContent());
		model.addAttribute("addressShippings", addressShippingDtos);
		model.addAttribute("carts", cartDtoList);
		model.addAttribute("isGuest", isGuest);

		return "user/shoping-cart";
	}

	@ResponseBody
	@PostMapping("/api/addToCart")
	public void addToCart(@RequestBody CartDto cartDto) throws NotFoundException {
		cartService.addToCart(cartDto);
	}

	@ResponseBody
	@PostMapping("/api/deleteCart/{id}")
	public void deleteCart(@PathVariable Long id, HttpSession session) {
		// ✅ Kiểm tra xem có phải guest không
		List<GuestCartDto> guestCart = (List<GuestCartDto>) session.getAttribute("guestCart");

		if (guestCart != null && !guestCart.isEmpty()) {
			// ✅ Xóa khỏi session của guest
			guestCart.removeIf(item -> item.getProductId().equals(id));
			session.setAttribute("guestCart", guestCart);
		} else {
			// ✅ Xóa khỏi DB của user đăng nhập
			cartService.deleteCart(id);
		}
	}

	@ResponseBody
	@PostMapping("/api/updateCart")
	public void updateCart(@RequestBody CartDto cartDto, HttpSession session) throws NotFoundException {
		// ✅ Kiểm tra xem có phải guest không
		List<GuestCartDto> guestCart = (List<GuestCartDto>) session.getAttribute("guestCart");

		if (guestCart != null && !guestCart.isEmpty()) {
			// ✅ Cập nhật session của guest
			for (GuestCartDto item : guestCart) {
				if (item.getProductId().equals(cartDto.getId())) {
					item.setQuantity(cartDto.getQuantity());
					break;
				}
			}
			session.setAttribute("guestCart", guestCart);
		} else {
			// ✅ Cập nhật DB của user đăng nhập
			cartService.updateCart(cartDto);
		}
	}
}