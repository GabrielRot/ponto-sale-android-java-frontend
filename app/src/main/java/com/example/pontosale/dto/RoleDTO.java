package com.example.pontosale.dto;

import java.util.List;

public class RoleDTO {

    private Long id;

    private String nome;

    private String descricao;

    private List<String> permissoesRole;

    public RoleDTO(Long id, String nome, String descricao, List<String> permissoesRole) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.permissoesRole = permissoesRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getPermissoesRole() {
        return permissoesRole;
    }

    public void setPermissoesRole(List<String> permissoesRole) {
        this.permissoesRole = permissoesRole;
    }
}
