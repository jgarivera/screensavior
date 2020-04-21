package me.jg.ss.main;

import java.util.Scanner;

public class ReversedBinary {

	public static void main(String[] args) {
		int number;

		Scanner in = new Scanner(System.in);

		System.out.println("Enter a positive integer");
		number = in.nextInt();

		if (number < 0) {
			System.out.println("Error: Not a positive integer");
		} else {

			System.out.print("Convert to binary is:");
			// System.out.print(binaryform(number));
			String s = printBinaryform(number);
			System.out.println(s);
		}
	}

	private static String printBinaryform(int number) {
		int remainder;
		String finalNumber = "";

		if (number <= 1) {
			finalNumber = finalNumber.concat(number + "");
			return finalNumber; // KICK OUT OF THE RECURSION
		}

		remainder = number % 2;
		printBinaryform(number >> 1);
		finalNumber = finalNumber.concat(remainder + "");
	}
}