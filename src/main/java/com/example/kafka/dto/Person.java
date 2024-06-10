package com.example.kafka.dto;


import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Person {
    private Long id;
    private String firstName;
    private String lastName;

    @OneToMany(mappedBy = "personId")
    private Set<Account> accounts;
}
