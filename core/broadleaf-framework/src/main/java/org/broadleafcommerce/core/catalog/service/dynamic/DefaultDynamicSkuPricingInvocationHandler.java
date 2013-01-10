/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.catalog.service.dynamic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.common.money.Money;

public class DefaultDynamicSkuPricingInvocationHandler implements InvocationHandler {

    private Sku delegate;
    private Money retailPrice;
    private Money salePrice;
    
    public DefaultDynamicSkuPricingInvocationHandler(Sku sku) {
        this.delegate = sku;
        try {
            Field retail = getSingleField(delegate.getClass(), "retailPrice");
            retail.setAccessible(true);
            Object retailVal = retail.get(delegate);
            retailPrice = retailVal==null?null:new Money((BigDecimal) retailVal);
            Field sale = getSingleField(delegate.getClass(), "salePrice");
            sale.setAccessible(true);
            Object saleVal = sale.get(delegate);
            salePrice = saleVal==null?null:new Money((BigDecimal) saleVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Field getSingleField(Class<?> clazz, String fieldName) throws IllegalStateException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsf) {
            // Try superclass
            if (clazz.getSuperclass() != null) {
                return getSingleField(clazz.getSuperclass(), fieldName);
            }

            return null;
        }
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getRetailPrice")) {
            return retailPrice;
        } else if (method.getName().equals("getSalePrice")) {
            return salePrice;
        } else {
            return method.invoke(delegate, args);
        }
    }

    public void reset() {
        delegate = null;
        retailPrice = null;
        salePrice = null;
    }
}
