package org.funcito.functionaljava;

import fj.F;

import org.funcito.internal.Invokable;

/**
 * Copyright 2011 Project Funcito Contributors
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MethodF<T,V> extends F<T,V> {
    private Invokable<T,V> invokable;

    public MethodF(Invokable<T,V> invokable) {
        this.invokable = invokable;
    }

    @Override
    public V f(T from) {
        return invokable.invoke(from);
    }
}
