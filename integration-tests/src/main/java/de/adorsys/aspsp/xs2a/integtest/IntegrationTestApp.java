/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.integtest;

import cucumber.api.cli.Main;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntegrationTestApp {
    public static void main(String[] args) throws Throwable {
//            SpringApplication.run(IntegrationTestApp.class, args);
        String [] arguments = new String[]{ "-g","classes.de.adorsys.aspsp.xs2a.integtest.stepdefinitions","classes/features"};
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        byte status = Main.run(arguments, contextClassLoader);
            // JUnitCore.main(CucumberIT.class.getCanonicalName());
    }
}
