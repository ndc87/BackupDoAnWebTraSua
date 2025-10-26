package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.AddressShipping.AddressShippingDto;
import com.project.DuAnTotNghiep.dto.AddressShipping.AddressShippingDtoAdmin;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.AddressShipping;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AddressShippingRepository;
import com.project.DuAnTotNghiep.repository.CustomerRepository;
import com.project.DuAnTotNghiep.security.CustomUserDetails;
import com.project.DuAnTotNghiep.service.AddressShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressShippingServiceImpl implements AddressShippingService {

    private final AddressShippingRepository addressShippingRepository;
    private final CustomerRepository customerRepository;

    public AddressShippingServiceImpl(AddressShippingRepository addressShippingRepository, CustomerRepository customerRepository) {
        this.addressShippingRepository = addressShippingRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<AddressShippingDto> getAddressShippingByAccountId() {
        Account currentAccount = getCurrentLogin();
        if (currentAccount == null) {
            // Guest không có địa chỉ đã lưu, trả về danh sách rỗng
            return new ArrayList<>();
        }
        
        List<AddressShipping> addressShippings = addressShippingRepository.findAllByCustomer_Account_Id(currentAccount.getId());
        List<AddressShippingDto> addressShippingDtos = new ArrayList<>();
        addressShippings.forEach(item -> {
            AddressShippingDto addressShippingDto = new AddressShippingDto();
            addressShippingDto.setId(item.getId());
            addressShippingDto.setAddress(item.getAddress());
            addressShippingDtos.add(addressShippingDto);
        });
        return addressShippingDtos;
    }

    @Override
    public AddressShippingDto saveAddressShippingUser(AddressShippingDto addressShippingDto) {
        Account currentAccount = getCurrentLogin();
        
        // Nếu là guest, chỉ trả về địa chỉ mà không lưu vào database
        if (currentAccount == null) {
            // Guest checkout - không lưu địa chỉ vào DB
            // Chỉ trả về địa chỉ để sử dụng cho đơn hàng hiện tại
            return addressShippingDto;
        }
        
        // User đã đăng nhập - kiểm tra giới hạn 5 địa chỉ
        List<AddressShipping> addressShippings = addressShippingRepository.findAllByCustomer_Account_Id(currentAccount.getId());
        if(addressShippings.size() >= 5) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Bạn chỉ được thêm tối đa 5 địa chỉ");
        }
        
        AddressShipping addressShipping = new AddressShipping();
        addressShipping.setAddress(addressShippingDto.getAddress());
        
        Customer customer = currentAccount.getCustomer();
        if (customer == null) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Tài khoản không có thông tin khách hàng");
        }
        addressShipping.setCustomer(customer);

        AddressShipping addressShippingNew = addressShippingRepository.save(addressShipping);
        return new AddressShippingDto(addressShippingNew.getId(), addressShippingNew.getAddress());
    }

    @Override
    public AddressShippingDto saveAddressShippingAdmin(AddressShippingDtoAdmin addressShippingDto) {
        AddressShipping addressShipping = new AddressShipping();
        // Sửa lỗi: dùng addressShippingDto.getAddress() thay vì addressShipping.getAddress()
        addressShipping.setAddress(addressShippingDto.getAddress());
        
        Customer customer = customerRepository.findById(addressShippingDto.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        addressShipping.setCustomer(customer);

        AddressShipping addressShippingNew = addressShippingRepository.save(addressShipping);
        return new AddressShippingDto(addressShippingNew.getId(), addressShippingNew.getAddress());
    }

    @Override
    public void deleteAddressShipping(Long id) {
        AddressShipping addressShipping = addressShippingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại"));
        addressShippingRepository.delete(addressShipping);
    }

    private Account getCurrentLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra authentication có null không hoặc chưa authenticated
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // Kiểm tra nếu principal là CustomUserDetails
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getAccount();
        }

        return null;
    }
}