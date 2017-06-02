package com.example.xg.myapplication;

/**
 * Created by XG on 2017/6/1.
 */
public class SourceBean extends BaseBean{
   private String SourcesSchool;
   private AimBean aimBean;

    public AimBean getAimBean() {
        return aimBean;
    }

    public void setAimBean(AimBean aimBean) {
        this.aimBean = aimBean;
    }

    public String getSourcesSchool() {
        return SourcesSchool;
    }

    public void setSourcesSchool(String sourcesSchool) {
        SourcesSchool = sourcesSchool;
    }
}
