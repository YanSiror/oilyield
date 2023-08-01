package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author Jing Yan
 * @Time: 31/7/2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassForget {
    private Integer id;
    private Integer uid;
    private Integer type;
    private String newpass;
}
