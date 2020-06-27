package com.keymao.security.springboot.model;

import lombok.Data;


@Data
public class PermissionDto {

    private String id;
    private String code;
    private String description;
    private String url;
}
