/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In Java, there is no direct support for the declaration of functional types
 * and functional variables. This deficiency is eliminated by the class
 * Function. In order to declare a function as an object of class Function, the
 * invoke method has to be overridden. This method accepts both separate input
 * parameters and a typed list holding them. The output of invoke is an object
 * of the given return type. Simplified versions of invoke exists suitable for
 * functions with none, one or two input parameters. It is important to mention
 * that in general only one of the invoke methods has to be overriden. The
 * invocation of a function is then triggered by simply calling invoke.
 *
 * <p>In combination with the powerful mechanism of anonymous classes, it is
 * also possible to build higher-order functions which is known from functional
 * programming (e.&nbsp;g.&nbsp;Haskel). The method compose shows how to
 * declare a function <tt>f</tt> which consists of <tt>h o (f1,...,fn)</tt>
 * where <tt>h</tt> is a function with <tt>n</tt> arguments and
 * <tt>f1,...,fn</tt> are functions with an equal number of arguments. Note
 * that compose provides the declaration of the function and does not execute
 * the function.</p>
 *
 * <p>Consider for example (see also the main-method) that you are interested
 * in building the function <tt>tan</tt> which can be build by composing
 * <tt>division</tt> and <tt>(sin, cos)</tt>. Function
 * <tt>div</tt>, <tt>sin</tt>, <tt>cos</tt>;
 * <code><pre>
 *   // Initialization of your functional objects
 *   ...
 *   // Declaration of a new function
 *   Function&lt;Double, Double&gt; tan = div.compose(sin, cos);
 *   ...
 *   // Execution of the new function
 *   tan.invoke(x);
 * </pre></code>
 * 
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 */
public abstract class Function<P, R> implements Serializable {

	/**
	 * A prototype function simply returns its arguments (identity-function).
	 */
	public static final Function<Object, Object> IDENTITY = new Function<Object, Object> () {

		/**
		 * Returns the argument itself (identity-function).
		 * 
		 * @param arguments the argument of the function.
		 * @return the <code>argument</code> is returned.
		 */
		public final Object invoke(List<?> arguments) {
			return arguments.size() == 1 ? arguments.get(0) : arguments;
		}
	};

	/**
	 * Returns the result of the function as an object of the return type. This
	 * method determines <code>arguments.size()</code> and calls the
	 * appropriate invoke method (see below). The other invoke methods call
	 * this method. This means, that the user either has to override this
	 * method or one (!) of the other invoke methods. If <code>null</code> is
	 * given as argument <code>invoke((P)null)</code> will be returned meaning
	 * <code>invoke(P)</code> is needed to be overriden.</p>
	 * 
	 * <p>Implementation:
	 * <code><pre>
	 *   if (arguments == null)
	 *       return invoke((P)null);
	 *   switch (arguments.size()) {
	 *       case 0:
	 *           return invoke();
	 *       case 1:
	 *           return invoke(arguments.get(0));
	 *       case 2:
	 *           return invoke(arguments.get(0), arguments.get(1));
	 *       default:
	 *           throw new RuntimeException("R invoke(List&lt;? super P&gt;) has to be overridden! The number of arguments was " + arguments.size() + ".");
	 *   }
	 * </pre></code></p>
	 * 
	 * @param arguments a lsit of the arguments to the function.
	 * @throws RuntimeException if a list of arguments is given that contains 3
	 *         or more arguments and the corresponding method
	 *         <code>R invoke(List&lt;? extends P&gt;)</code> has not been
	 *         overridden.
	 * @return the function value is returned.
	 */
	public R invoke(List<? extends P> arguments) throws RuntimeException {
		if (arguments == null)
			return invoke((P)null);
		switch (arguments.size()) {
			case 0:
				return invoke();
			case 1:
				return invoke(arguments.get(0));
			case 2:
				return invoke(arguments.get(0), arguments.get(1));
			default:
				throw new RuntimeException("R invoke(List<? super P>) has to be overridden! The number of arguments was " + arguments.size() + ".");
		}
	}

	/**
	 * Returns the result of the function as an object of the result type.
	 * 
	 * <p>Implementation:
	 * <code><pre>
	 *   return invoke(new ArrayList&lt;P&gt;(0));
	 * </pre></code></p>
	 * 
	 * @return the function value is returned.
	 */
	public R invoke() {
		return invoke(new ArrayList<P>(0));
	}

	/**
	 * Returns the result of the function as an object of the result type.
	 * 
	 * <p>Implementation:
	 * <code><pre>
	 *   return invoke(Arrays.asList(argument));
	 * </pre></code></p>
	 * 
	 * @param argument the argument to the function.
	 * @return the function value is returned.
	 */
	public R invoke(P argument) {
		return invoke(Arrays.asList(argument));
	}

	/**
	 * Returns the result of the function as an object of the result type.
	 * 
	 * <p>Implementation:
	 * <code><pre>
	 *   return invoke(Arrays.asList(argument0, argument1));
	 * </pre></code></p>
	 * 
	 * @param argument0 the first argument to the function
	 * @param argument1 the second argument to the function
	 * @return the function value is returned
	 */
	public R invoke(P argument0, P argument1) {
		return invoke(Arrays.asList(argument0, argument1));
	}

	/**
	 * This method declares a new function <tt>h</tt> by composing a number of
	 * functions <tt>f_1,...,f_n</tt> with this function <tt>g</tt>.
	 * 
	 * <p><b>Note</b> that the method does not execute the code of the new
	 * function <tt>h</tt>. The invocation of the composed function <tt>h</tt>
	 * is just triggered by a call of its own <tt>invoke-method</tt>. Then, the
	 * input parameters are passed to each given function <tt>f_1,...,f_n</tt>,
	 * and the returned objects are used as the input parameters of the new
	 * function <tt>h</tt>. The invoke method of <tt>h</tt> returns the final
	 * result.</p>
	 * 
	 * @param <T> the type of the composed function's parameters.
	 * @param functions the functions to be concatenated with this function.
	 * @return the result of the composition.
	 */
	public <T> Function<T, R> compose(Function<? super T, ? extends P>... functions) {
		final Function<P, R> outer = this;
		final Function<T, List<P>> inner = new DecoratorArrayFunction<T, P>(functions);
		return new Function<T, R>() {
			public R invoke(List<? extends T> objects) {
				return outer.invoke(inner.invoke(objects));
			}
		};
	}

    public <T> Function<T, R> composeUnary(final Function<? super T, ? extends P> f) {
        final Function<P, R> outer = this;
        return new Function<T, R>() {
            public R invoke (final T object) {
                return outer.invoke(f.invoke(object));
            }
        };
    }

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		Function<Double, Double> sin = new Function<Double, Double>() {
			public Double invoke(Double argument) {
				return Math.sin(argument);
			}
		};
		Function<Double, Double> cos = new Function<Double, Double>() {
			public Double invoke(Double argument) {
				return Math.cos(argument);
			}
		};
		Function<Double, Double> tan = new Function<Double, Double>() {
			public Double invoke(Double argument) {
				return Math.tan(argument);
			}
		};
		Function<Double, Double> div = new Function<Double, Double>() {
			public Double invoke(Double dividend, Double divisor) {
				return dividend / divisor;
			}
		};
		Double dv = 0.5;
		if (args.length == 1)
			dv = new Double(args[0]);
			
		System.out.println("parameter value: " + dv);
		
		System.out.println("sin: " + sin.invoke(dv));
		System.out.println("cos: " + cos.invoke(dv));

		double resTan = tan.invoke(dv);
		double resComposition = div.compose(sin, cos).invoke(dv);
		
		System.out.println("tan: " + resTan);
		System.out.println("div.compose(sin,cos): " + resComposition);
		
		if (Math.abs(resTan - resComposition) > 1E-12)
			throw new RuntimeException("Something is wrong with function composition");
	}
}