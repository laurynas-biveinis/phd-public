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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Println-Function prints arguments to a specified print stream and
 * returns them. If no print stream is specified, the standard out is taken as
 * default. If an array of arguments is given the single objects will be
 * separated by a given delimiter. Println-Functions can be used to log a
 * composition of functions at a specified level. To do so just a
 * Prinln-Function has to be inserted at the right place like
 * <code><pre>
 *   Function&lt;Number, Double&gt; tan = div.compose(new Println&lt;Double&gt;().compose(sin), cos);
 * </pre></code>
 * instead of 
 * <code><pre>
 *   Function&lt;Number, Double&gt; tan = div.compose(sin, cos);
 * </pre></code>
 * 
 * @param <T> the parameter type as well as the return type of this function.
 */
public class Println<T> extends Function<T, T> {

	/** Default Function for Println using System.out as
	 * {@link java.io.PrintStream PrintStream} and space as delimeter.
	 */
	public static final Println<Object> DEFAULT_INSTANCE = new Println<Object>();

	/**
	 * The {@link java.io.PrintStream PrintStream} used for output.
	 */
	protected PrintStream printStream;

	/**
	 * The used delimeter to separate the given arguments.
	 */
	protected String delimiter;

	/**
	 * Constructs a new Println-Function.
	 * 
	 * @param printStream {@link java.io.PrintStream PrintStream} using for the
	 *        output.
	 * @param delimiter delimiter used for separating array-arguments.
	 */
	public Println(PrintStream printStream, String delimiter) {
		this.printStream = printStream;
		this.delimiter = delimiter;
	}

	/**
	 * Constructs a new Println-Function using a space as delimeter.
	 * 
	 * @param printStream {@link java.io.PrintStream PrintStream} using for the
	 *        output.
	 */
	public Println(PrintStream printStream) {
		this(printStream, " ");
	}

	/**
	 * Constructs a new Println-Function using a space as delimeter and
	 * <code>System.out</code> as output.
	 */
	public Println() {
		this(System.out);
	}

	/**
	 * Prints the given arguments to a {@link java.io.PrintStream PrintStream}
	 * and returns the arguments.
	 * 
	 * @param arguments the arguments to print.
	 * @return the arguments given.
	 */
	public T invoke(List<? extends T> arguments) {
		// arrays of length 0? -> just doing 'newLine'
		if (arguments == null || arguments.size() == 0)
			printStream.println();
		else {
			// if just a single object is given, return this single object itself
			if (arguments.size() == 1)
				return invoke(arguments.get(0));
			// else process the given array
			for (int i = 0; i < arguments.size()-1; i++) {
				printStream.print(arguments.get(i));
				printStream.print(delimiter);
			}
			printStream.println(arguments.get(arguments.size()-1));
		}
		return (T)arguments;
	}

	/**
	 * Prints the given argument to a {@link java.io.PrintStream PrintStream}
	 * and returns the argument itself.
	 * 
	 * @param argument the arguments to print.
	 * @return the argument given.
	 */
	public T invoke(T argument) {
		printStream.println(argument);
		return argument;
	}

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		Function<Number, Double> sin = new Function<Number, Double>() {
			public Double invoke(Number x) {
				return Math.sin(x.doubleValue());
			}
		};
		Function<Number, Double> cos = new Function<Number, Double>() {
			public Double invoke(Number x) {
				return Math.cos(x.doubleValue());
			}
		};
		Function<Number, Double> div = new Function<Number, Double>() {
			public Double invoke(Number dividend, Number divisor) {
				return dividend.doubleValue() / divisor.doubleValue();
			}
		};
		Println<Double> println = new Println<Double>();
		
		System.out.print("Printing result of sin at computation:\nsin=");
		Function<Number, Double> tan = div.compose(println.compose(sin), cos);
		System.out.println("tan(0.5)=" + tan.invoke(0.5));
		//
		System.out.println("----------------------------\nJust printing an array:");
		println.invoke(Arrays.asList(1.0, 2.0, 3.0));
		//
		System.out.println("----------------------------\nJust printing array of length 0:");
		println.invoke(new ArrayList<Double>(0));
		//
		System.out.println("----------------------------\nJust printing null:");
		println.invoke((Double)null);
	}
	
}