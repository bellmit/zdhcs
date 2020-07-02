package net.northking.atp.entity;

import java.io.Serializable;

public class DoubleBarEchartEntity implements Serializable {

    private String catalog;

    private String name;

    private Long leftBar;

    private Long rightBar;

    public DoubleBarEchartEntity() {
    }

    public DoubleBarEchartEntity(String catalog, String name, Long leftBar, Long rightBar) {
        this.catalog = catalog;
        this.name = name;
        this.leftBar = leftBar;
        this.rightBar = rightBar;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeftBar() {
        return leftBar;
    }

    public void setLeftBar(Long leftBar) {
        this.leftBar = leftBar;
    }

    public Long getRightBar() {
        return rightBar;
    }

    public void setRightBar(Long rightBar) {
        this.rightBar = rightBar;
    }

    @Override
    public String toString() {
        return "DoubleBarEchartEntity{" +
                "catalog='" + catalog + '\'' +
                ", name='" + name + '\'' +
                ", leftBar='" + leftBar + '\'' +
                ", rightBar='" + rightBar + '\'' +
                '}';
    }
}
