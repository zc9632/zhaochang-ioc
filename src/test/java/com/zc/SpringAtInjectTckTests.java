/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zc;

import com.zc.support.ApplicationContext;
import com.zc.test.bean.tck.Tck;
import com.zc.test.bean.tck.auto.*;
import com.zc.test.bean.tck.auto.accessories.Cupholder;
import com.zc.test.bean.tck.auto.accessories.SpareTire;
import junit.framework.Test;

/**
 * @author Juergen Hoeller
 * @since 3.0
 */
public class SpringAtInjectTckTests {

	@SuppressWarnings("unchecked")
	public static Test suite() {
		ApplicationContext ac = new ApplicationContext();
//		AnnotatedBeanDefinitionReader bdr = new AnnotatedBeanDefinitionReader(ac);
//		bdr.setScopeMetadataResolver(new Jsr330ScopeMetadataResolver());
		ac.registerBean(Convertible.class);
		ac.registerBean(DriversSeat.class);
		ac.registerBean(Seat.class);
		ac.registerBean(V8Engine.class);
		ac.registerBean(SpareTire.class, "spare");
		ac.registerBean(Cupholder.class);
		ac.registerBean(Tire.class);
		ac.registerBean(FuelTank.class);
		Car car = ac.getBean(Car.class);

		return Tck.testsFor(car, false, true);
	}

}
