/*
 * Copyright 2011 Project Funcito Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.funcito.example.play;

import java.util.Arrays;
import java.util.List;

public class MySubClass extends MyClass {

    public MySubClass(String myString) {
        super(myString);
    }

    @Override
    public String getMyString() {
        return super.getMyString() + "++";
    }

    public static void main(String[] args) throws Throwable {
        MyClass m1 = new MySubClass("A");
        MyClass m2 = new MySubClass("B");
        MyClass m3 = null;

        List<MyClass> list = Arrays.asList(m1,m2,m3);
        demoOptions(list);
    }
}
