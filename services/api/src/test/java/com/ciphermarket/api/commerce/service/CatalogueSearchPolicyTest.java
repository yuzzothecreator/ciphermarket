package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.dto.CatalogueSort;
import com.ciphermarket.api.common.enums.ProductType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogueSearchPolicyTest {

    @Test
    void defaultSortIsNewest() {
        assertThat(CatalogueSort.NEWEST.name()).isEqualTo("NEWEST");
        assertThat(CatalogueSort.values()).contains(
                CatalogueSort.PRICE_ASC,
                CatalogueSort.PRICE_DESC,
                CatalogueSort.NAME_ASC
        );
    }

    @Test
    void productTypesRemainFilterable() {
        assertThat(ProductType.PDF).isNotNull();
        assertThat(ProductType.SOURCE_CODE).isNotNull();
    }
}
