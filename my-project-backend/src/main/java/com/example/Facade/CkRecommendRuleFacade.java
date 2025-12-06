package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.dto.RecommendRule;
import com.example.entity.cangku.dto.RecommendRuleItem;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.ReCommendRuleDetailResp;
import com.example.entity.cangku.resp.ReCommendRulePageListResp;
import com.example.entity.cangku.resp.RecommendRuleItemDetailResp;
import com.example.service.*;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:37
 */
@Service
public class CkRecommendRuleFacade {
    @Resource
    private CkRecommendRuleService recommendRuleService;
    @Resource
    private CkProductService productService;
    @Resource
    private CkCustomerService customerService;
    @Resource
    private CkCustomerSkuMappingService customerSkuMappingService;
    @Resource
    private CkRecommendRuleItemService recommendRuleItemService;


    public Page<ReCommendRulePageListResp> pageList(Page<RecommendRule> page, RecommendRuleListPageReq req) {
        Page<RecommendRule> list = recommendRuleService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> triggerProductIds = list.getRecords().stream().map(v -> v.getTriggerProductId()).distinct().collect(Collectors.toList());
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Product> products = productService.selectByIds(req.getTenantId(), triggerProductIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        List<Long> ruleIds = list.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        List<RecommendRuleItem> ruleItems = recommendRuleItemService.selectByRuleIds(req.getTenantId(), ruleIds);
        Map<Long,Long> ruleId2CountMap =ruleItems.stream().collect(Collectors.groupingBy(RecommendRuleItem::getRuleId, Collectors.counting()));


        List<Long> customerIds = list.getRecords().stream().map(v -> v.getCustomerId()).distinct().collect(Collectors.toList());
        List<Customer> customers = customerService.selectByTenantIdAndCustomerIds(req.getTenantId(), customerIds);
        Map<Long, Customer> customerId2CustomerMap = customers.stream().collect(Collectors.toMap(Customer::getId, v -> v));

        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ReCommendRulePageListResp> collect = list.getRecords().stream().map(v -> {
            ReCommendRulePageListResp p = new ReCommendRulePageListResp();
            BeanUtils.copyProperties(v, p);

            if (finalProductId2ProductMap.containsKey(v.getTriggerProductId())) {
                Product product = finalProductId2ProductMap.get(v.getTriggerProductId());
                p.setTriggerProductName(product.getName());
                p.setTriggerProductColor(product.getColor());
                p.setTriggerProductSpec(product.getSpec());
            }

            if (customerId2CustomerMap.containsKey(v.getCustomerId())) {
                Customer customer = customerId2CustomerMap.get(v.getCustomerId());
                p.setCustomerName(customer.getCustomerName());
            }

            p.setItemCount(ruleId2CountMap.getOrDefault(v.getId(),0L));

            return p;
        }).collect(Collectors.toList());

        Page<ReCommendRulePageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean delete(CommendRuleDeleteReq req) {
        RecommendRule p = recommendRuleService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("sku映射不存在");
        }

        RecommendRule save = new RecommendRule();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return recommendRuleService.updateById(save);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean create(RecommendRuleCreateReq req) {
        if (req == null || CollectionUtils.isEmpty(req.getRuleItems())) {
            throw new ValidationException("参数错误");
        }
        RecommendRule r = new RecommendRule();
        r.setTenantId(req.getTenantId());
        r.setRuleCode(req.getRuleCode());
        r.setRuleName(req.getRuleName());
        r.setCustomerId(req.getCustomerId());
        r.setTriggerProductId(req.getTriggerProductId());
        r.setTriggerMinQuantity(req.getTriggerMinQuantity());
        r.setTriggerMaxQuantity(req.getTriggerMaxQuantity());
        r.setPriority(req.getPriority());
        r.setStatus(req.getStatus());
        r.setApplyScene(req.getApplyScene());
        r.setRuleType(req.getRuleType());
        r.setConfidence(req.getConfidence());
        r.setRemark(req.getRemark());
        r.setCreatedAt(new Date());
        r.setCreatedBy(req.getUserId());
        r.setModifiedAt(new Date());
        r.setModifiedBy(req.getUserId());
        boolean save = recommendRuleService.save(r);
        if (!save) {
            throw new ValidationException("保存失败");
        }
        List<RecommendRuleItem> recommendRuleItemStream = req.getRuleItems().stream().map(v -> {
            RecommendRuleItem item = new RecommendRuleItem();
            item.setRuleId(r.getId());
            item.setProductId(v.getProductId());
            item.setTenantId(req.getTenantId());
            item.setQuantityType(v.getQuantityType());
            item.setQuantityValue(v.getQuantityValue());
            item.setIsRequired(v.getIsRequired());
            item.setConfidence(v.getConfidence());
            item.setSequence(v.getSequence());
            item.setCreatedAt(new Date());
            item.setModifiedAt(new Date());
            item.setCreatedBy(req.getUserId());
            item.setModifiedBy(req.getUserId());
            return item;
        }).collect(Collectors.toList());
        return recommendRuleItemService.saveBatch(recommendRuleItemStream);
    }

    public Boolean update(RecommendRuleCreateReq req) {
        RecommendRule wh = recommendRuleService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("规则不存在，请新增！");
        }
        RecommendRule save = new RecommendRule();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        boolean b = recommendRuleService.updateById(save);
        if (!b) {
            throw new ValidationException("更新失败");
        }
        Boolean removeResult = recommendRuleItemService.delectedByRuleId(req.getId(), req.getUserId(),req.getTenantId());
        if (!removeResult) {
            throw new ValidationException("删除失败");
        }

        List<RecommendRuleItem> recommendRuleItemStream = req.getRuleItems().stream().map(v -> {
            RecommendRuleItem item = new RecommendRuleItem();
            item.setRuleId(save.getId());
            item.setTenantId(req.getTenantId());
            item.setProductId(v.getProductId());
            item.setQuantityType(v.getQuantityType());
            item.setQuantityValue(v.getQuantityValue());
            item.setIsRequired(v.getIsRequired());
            item.setConfidence(v.getConfidence());
            item.setSequence(v.getSequence());
            item.setCreatedAt(new Date());
            item.setModifiedAt(new Date());
            item.setCreatedBy(req.getUserId());
            item.setModifiedBy(req.getUserId());
            return item;
        }).collect(Collectors.toList());
        return recommendRuleItemService.saveBatch(recommendRuleItemStream);
    }

    public Boolean updateStatus(RecommendRuleUpdateStatusReq req) {
        RecommendRule wh = recommendRuleService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("规则不存在");
        }

        RecommendRule save = new RecommendRule();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return recommendRuleService.updateById(save);
    }

    public ReCommendRuleDetailResp detail(Long id, Long tenantId) {
        RecommendRule recommendRule = recommendRuleService.selectById(id, tenantId);
        if (recommendRule == null) {
            throw new ValidationException("规则不存在");
        }
        ReCommendRuleDetailResp result = new ReCommendRuleDetailResp();
        BeanUtils.copyProperties(recommendRule, result);

        List<RecommendRuleItem> recommendRuleItems = recommendRuleItemService.selectByRuleIds(tenantId, Lists.newArrayList(id));

        List<Long> productIds = recommendRuleItems.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        result.setRuleItems(recommendRuleItems.stream().map(v -> {
            RecommendRuleItemDetailResp item = new RecommendRuleItemDetailResp();
            BeanUtils.copyProperties(v, item);

            if (finalProductId2ProductMap.containsKey(v.getProductId())) {
                Product product = finalProductId2ProductMap.get(v.getProductId());
                item.setProductName(product.getName());
                item.setProductSpec(product.getSpec());
                item.setProductColor(product.getColor());
                item.setProductId(product.getId());
                item.setProductSku(product.getSku());
            }
            return item;
        }).collect(Collectors.toList()));
        return result;
    }

    public List<RecommendRuleItemDetailResp> items(Long ruleId, Long tenantId) {
        RecommendRule recommendRule = recommendRuleService.selectById(ruleId, tenantId);
        if (recommendRule == null) {
            throw new ValidationException("规则不存在");
        }
        List<RecommendRuleItem> recommendRuleItems = recommendRuleItemService.selectByRuleIds(tenantId, Lists.newArrayList(ruleId));

        List<Long> productIds = recommendRuleItems.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<RecommendRuleItemDetailResp> collect = recommendRuleItems.stream().map(v -> {
            RecommendRuleItemDetailResp item = new RecommendRuleItemDetailResp();
            BeanUtils.copyProperties(v, item);

            if (finalProductId2ProductMap.containsKey(v.getProductId())) {
                Product product = finalProductId2ProductMap.get(v.getProductId());
                item.setProductName(product.getName());
                item.setProductSpec(product.getSpec());
                item.setProductColor(product.getColor());
                item.setProductId(product.getId());
                item.setProductSku(product.getSku());
            }
            return item;
        }).collect(Collectors.toList());
        return collect;
    }
}
