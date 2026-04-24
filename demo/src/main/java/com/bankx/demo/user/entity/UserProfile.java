package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "user_profiles",
        indexes = {
                @Index(name = "idx_user_profiles_user_id", columnList = "user_id", unique = true)
        })
@SQLRestriction("deleted = false")
@Schema(description = "Customer personal and contact information")
@RequiredArgsConstructor
public class UserProfile extends BaseEntity {

    //—— Relationships ——————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, unique = true)
    private User user;

    //—— LegalName ——————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    @Column(name = "first_name", nullable = false, length = 50)
    @Schema(description = "First name")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @Schema(description = "Last name")
    private String lastName;

    //—— Contact —————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    @Column(nullable = false, length = 20)
    @Schema(description = "Phone number")
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    @Schema(description = "Date of birth")
    private LocalDate birthDate;

    //—— Address ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————

    @Column(name = "address_line1", nullable = false, length = 100)
    @Schema(description = "Address line 1")
    private String addressLine1;

    @Column(name = "address_line2", length = 100)
    @Schema(description = "Address line 2")
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 50)
    @Schema(description = "City")
    private String city;

    @Column(name = "state", nullable = false, length = 2)
    @Schema(description = "State")
    private String state;

    @Column(name = "country", nullable = false, length = 2)
    @Schema(description = "Country")
    private String country;

    @Column(name = "zip_code", nullable = false, length = 10)
    @Schema(description = "Postal code")
    private String postalCode;

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public String getFormattedAddress(){
        StringBuilder sb = new StringBuilder();
        if(!StringUtils.isEmpty(addressLine1)) sb.append(addressLine1);
        if(!StringUtils.isEmpty(addressLine2)) sb.append(", ").append(addressLine2);
        if(!StringUtils.isEmpty(city)) sb.append(", ").append(city);
        if(!StringUtils.isEmpty(state)) sb.append(", ").append(state);
        if(!StringUtils.isEmpty(country)) sb.append(", ").append(country);
        if(!StringUtils.isEmpty(postalCode)) sb.append(", ").append(postalCode);
        return sb.toString();
    }

}
