package org.threefour.ddip.product.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.threefour.ddip.audit.BaseGeneralEntity;
import org.threefour.ddip.member.domain.Member;
import org.threefour.ddip.product.category.domain.ProductCategory;
import org.threefour.ddip.product.exception.UpdateFormNoValueException;
import org.threefour.ddip.product.priceinformation.domain.PriceInformation;
import org.threefour.ddip.util.FormatValidator;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;
import static org.threefour.ddip.product.exception.ExceptionMessage.UPDATE_FORM_NO_VALUE_EXCEPTION_MESSAGE;

@Entity
@NoArgsConstructor(access = PROTECTED)
public class Product extends BaseGeneralEntity {
    @Column(nullable = false, length = 20)
    private String name;

    @Convert(converter = Price.PriceConverter.class)
    @Column(nullable = false)
    private Price price;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    @OneToMany(mappedBy = "product", cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true, fetch = LAZY)
    @JsonManagedReference
    private List<ProductCategory> productCategories;

    @OneToMany(mappedBy = "product", cascade = {PERSIST, MERGE, REMOVE}, fetch = LAZY)
    private List<PriceInformation> priceInformation;

    @Builder
    private Product(
            Long id, String name, String price, String title, String content, Member seller
    ) {
        this.name = name;
        this.price = Price.of(price);
        this.title = title;
        this.content = content;
        this.seller = seller;
    }

    public static Product from(RegisterProductRequest registerProductRequest, Member seller) {
        return Product.builder()
                .name(registerProductRequest.getName())
                .price(registerProductRequest.getPrice())
                .title(registerProductRequest.getTitle())
                .content(registerProductRequest.getContent())
                .seller(seller)
                .build();
    }

    public String getName() {
        return name;
    }

    public Price getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Member getSeller() {
        return seller;
    }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    List<PriceInformation> getPriceInformation() {
        return priceInformation;
    }

    public void update(UpdateProductRequest updateProductRequest) {
        String nameToUpdate = updateProductRequest.getName();
        if (FormatValidator.hasValue(nameToUpdate)) {
            name = nameToUpdate;
            return;
        }

        String priceToUpdate = updateProductRequest.getPrice();
        if (FormatValidator.hasValue(priceToUpdate)) {
            price = Price.of(priceToUpdate);
            return;
        }

        String titleToUpdate = updateProductRequest.getTitle();
        if (FormatValidator.hasValue(titleToUpdate)) {
            title = titleToUpdate;
            return;
        }

        String contentToUpdate = updateProductRequest.getContent();
        if (FormatValidator.hasValue(contentToUpdate)) {
            content = contentToUpdate;
            return;
        }

        AutoDiscountRequest autoDiscountRequest = updateProductRequest.getAutoDiscountRequest();
        if (FormatValidator.hasValue(autoDiscountRequest)) {
            if (FormatValidator.hasValue(priceInformation)) {
                PriceInformation lastPriceInformation = priceInformation.get(priceInformation.size() - 1);
                lastPriceInformation.update(autoDiscountRequest);
                return;
            }

            priceInformation.add(PriceInformation.from(this, autoDiscountRequest));
            return;
        }

        throw new UpdateFormNoValueException(UPDATE_FORM_NO_VALUE_EXCEPTION_MESSAGE);
    }

    public void delete() {
        deleteEntity();
    }

    @PostLoad
    private void init() {
        Hibernate.initialize(seller);
        Hibernate.initialize(productCategories);
    }
}
