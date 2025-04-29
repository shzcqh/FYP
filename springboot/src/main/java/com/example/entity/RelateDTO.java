package com.example.entity;
//来把 “某个用户 – 对某部电影的兴趣程度（指数）” 这一行信息打包好//
public class RelateDTO {
    /** 用户id */
    private Integer useId;
    /** 物品id */
    private Integer goodsId;
    /** 指数 */
    private Integer index;

    public RelateDTO(Integer useId, Integer goodsId, Integer index) {
        this.useId = useId;
        this.goodsId = goodsId;
        this.index = index;
    }

    public Integer getUseId() {
        return useId;
    }

    public void setUseId(Integer useId) {
        this.useId = useId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
