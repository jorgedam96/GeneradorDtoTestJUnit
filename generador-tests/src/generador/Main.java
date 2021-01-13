
package generador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author jsegadem
 *
 */
public class Main {
	private static int generados = 0;
	private static String generar;
	// ruta paquete generador
	private static final String paquete = "C:\\Users\\jsegadem\\Downloads\\DARWIN\\generador-tests\\src\\generador";

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		System.out.println("Generar test para equals() y hashCode() sobreescritos? s/n:");
		generar = scanner.nextLine().toLowerCase().trim();

		scanner.close();
		ArrayList<String[]> clases;

		final File folder = new File(paquete);

		File myObj = new File(paquete + "/output.txt");
		if (myObj.delete()) {
			System.out.println("Borrado el fichero: " + myObj.getName());
		} else {
			System.out.println("No se ha borrado el fichero");
		}

		clases = listFilesForFolder(folder);

		for (int i = 0; i < clases.size(); i++) {

			System.err.println("Clase: " + clases.get(i)[0]);
			System.out.println("\t Ruta: " + clases.get(i)[1]);

			try (FileReader fr = new FileReader(clases.get(i)[1]); BufferedReader br = new BufferedReader(fr);) {

				Object[] objects = procesarFichero(fr, br);
				generarTest((String) objects[0], (ArrayList<String>) objects[1], (ArrayList<String>) objects[2]);

			} catch (Exception e) {
				System.out.println("Excepcion leyendo fichero " + clases.get(i)[1] + ": " + e);
				e.printStackTrace();
			}
		}
		System.err.println("Test generados: " + generados);
	}

	public static ArrayList<String[]> listFilesForFolder(final File folder) {
		int cont = 0;
		ArrayList<String[]> clases = new ArrayList<>();
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.getName().equals("Main.java") && !fileEntry.getName().equals("output.txt")) {
				String[] v = new String[2];

				v[0] = fileEntry.getName();
				v[1] = fileEntry.getAbsolutePath();
				clases.add(v);
				System.err.println("*Clase encontrada: " + Arrays.toString(v));
				cont++;
			}
		}

		System.err.println("\nTotal: " + cont + "\n");

		return clases;
	}

	private static void generarTest(String clase, ArrayList<String> atributos, ArrayList<String> tipos) {
		if (atributos == null || atributos.size() == 0) {
			System.err.println("La clase " + clase + " no tiene atributos o es final");
		} else {

			String claseMin = clase;
			claseMin = claseMin.replace(claseMin.charAt(0), Character.toLowerCase(claseMin.charAt(0)));

			try (BufferedWriter output = new BufferedWriter(new FileWriter(paquete + "/output.txt", true))) {

				output.write("@Test");
				output.newLine();
				output.write("public void test" + clase + "(){");
				output.newLine();
				output.newLine();

				output.write(clase);
				output.write(" ");
				output.write(claseMin);
				output.write(" = new ");
				output.write(clase);
				output.write("();");

				for (int i = 0; i < atributos.size(); i++) {
					output.newLine();
					output.write(claseMin);
					output.write(".set");

					if (atributos.get(i).toLowerCase().startsWith("is")) {
						output.write(atributos.get(i).substring(2, 3).toUpperCase() + atributos.get(i).substring(3));

					} else {
						output.write(atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
					}
					if (tipos.get(i).equals("boolean")) {
						output.write("(false);");
					} else {
						output.write("(null);");
					}
				}

				String claseMin2 = claseMin + "2";
				output.newLine();
				output.newLine();

				output.write(clase);
				output.write(" ");
				output.write(claseMin2);
				output.write(" = new ");
				output.write(clase);
				output.write("();");

				for (int i = 0; i < atributos.size(); i++) {

					output.newLine();
					output.write(claseMin2);
					output.write(".set");
					if (atributos.get(i).toLowerCase().startsWith("is")) {
						output.write(atributos.get(i).substring(2, 3).toUpperCase() + atributos.get(i).substring(3));

					} else {
						output.write(atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
					}
					output.write("(");
					output.write(claseMin);

					if (tipos.get(i).equalsIgnoreCase("boolean")) {
						if (atributos.get(i).toLowerCase().startsWith("is")) {
							output.write("." + atributos.get(i));

						} else {
							output.write(".is" + atributos.get(i).substring(0, 1).toUpperCase()
									+ atributos.get(i).substring(1));

						}
					} else {
						output.write(".get");
						output.write(atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));

					}

					output.write("());");

				}
				output.newLine();
				output.write("assertNotNull(" + claseMin + ".toString());");
				output.newLine();

				if (!generar.equals("s")) {
					System.err.println("Se ha elegido no generar equals");

				} else if (tipos == null || tipos.size() == 0) {
					System.err.println("Fallo en los tipos de datos");

				} else {

					output.newLine();

					output.write("assertEquals(" + claseMin + ", " + claseMin2 + ");");
					output.newLine();
					output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
					output.newLine();
					output.write("assertTrue(" + claseMin + ".equals( " + claseMin + "));");
					output.newLine();
					output.write("assertFalse(" + claseMin + ".equals(null));");
					output.newLine();
					output.write("assertFalse(" + claseMin + ".equals(new StringBuilder()));");
					output.newLine();
					output.write("assertNotNull(" + claseMin + ".hashCode());");
					output.newLine();

					for (int i = 0; i < tipos.size(); i++) {
						if (Character.isUpperCase(tipos.get(i).charAt(0))) {

							output.newLine();
							output.write(claseMin2);
							output.write(".set");
							if (atributos.get(i).toLowerCase().startsWith("is")) {
								output.write(
										atributos.get(i).substring(2, 3).toUpperCase() + atributos.get(i).substring(3));

							} else {
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
							}
							inicializar(tipos, output, i, true);

							output.newLine();
							output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
							output.newLine();
							output.write(tipos.get(i) + " obj" + i + " =");
							inicializar(tipos, output, i, false);
							output.newLine();

							output.write(claseMin);
							output.write(".set");
							if (atributos.get(i).toLowerCase().startsWith("is")) {
								output.write(
										atributos.get(i).substring(2, 3).toUpperCase() + atributos.get(i).substring(3));

							} else {
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
							}
							output.write("(obj" + i + ");");
							output.newLine();
							output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
							output.newLine();

							output.newLine();
							output.write(claseMin2);
							output.write(".set");
							if (atributos.get(i).toLowerCase().startsWith("is")) {
								output.write(
										atributos.get(i).substring(2, 3).toUpperCase() + atributos.get(i).substring(3));

							} else {
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
							}
							output.write("(obj" + i + ");");
							output.newLine();
							output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
							output.newLine();

						} else {
							switch (tipos.get(i)) {
							case "int": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(0);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(1);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(1);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "char": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("('');");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("('1');");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("('1');");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "boolean": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								if (atributos.get(i).toLowerCase().startsWith("is")) {
									output.write(atributos.get(i).substring(2, 3).toUpperCase()
											+ atributos.get(i).substring(3));

								} else {
									output.write(atributos.get(i).substring(0, 1).toUpperCase()
											+ atributos.get(i).substring(1));
								}
								output.write("(true);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								if (atributos.get(i).toLowerCase().startsWith("is")) {
									output.write(atributos.get(i).substring(2, 3).toUpperCase()
											+ atributos.get(i).substring(3));

								} else {
									output.write(atributos.get(i).substring(0, 1).toUpperCase()
											+ atributos.get(i).substring(1));
								}
								output.write("(false);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								if (atributos.get(i).toLowerCase().startsWith("is")) {
									output.write(atributos.get(i).substring(2, 3).toUpperCase()
											+ atributos.get(i).substring(3));

								} else {
									output.write(atributos.get(i).substring(0, 1).toUpperCase()
											+ atributos.get(i).substring(1));
								}
								output.write("(false);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "float": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(0f);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(1f);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("(1f);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "double": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((double)0);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((double)1);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((double)1);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "byte": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((byte)0);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((byte)1);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((byte)1);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "short": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((short)0);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((short)1);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((short)1);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							case "long": {
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((long)0);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((long)1);");
								output.newLine();
								output.write("assertFalse(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								output.write(claseMin2);
								output.write(".set");
								output.write(
										atributos.get(i).substring(0, 1).toUpperCase() + atributos.get(i).substring(1));
								output.write("((long)1);");
								output.newLine();
								output.write("assertTrue(" + claseMin + ".equals( " + claseMin2 + "));");
								output.newLine();
								break;
							}
							}

						}
					}
					output.newLine();
					output.write("assertNotNull(" + claseMin + ".hashCode());");
					output.newLine();
				}

				output.newLine();
				output.write("}");
				output.newLine();
				generados++;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * @param tipos
	 * @param output
	 * @param i
	 * @throws IOException
	 */
	private static void inicializar(ArrayList<String> tipos, BufferedWriter output, int i, boolean parentesis)
			throws IOException {
		if (parentesis) {
			if (tipos.get(i).contains("List")) {
				output.write("(new ArrayList<>());");

			} else if (tipos.get(i).contains("Map")) {
				output.write("(new HashMap<>());");

			} else if (tipos.get(i).contains("String")) {
				output.write("(\"\");");

			} else if (tipos.get(i).contains("Long")) {
				output.write("(0l);");

			} else if (tipos.get(i).contains("Integer")) {
				output.write("(0);");

			} else if (tipos.get(i).contains("Boolean")) {
				output.write("(false);");

			} else if (tipos.get(i).contains("Double")) {
				output.write("((double)0);");

			} else {
				output.write("(new " + tipos.get(i) + "());");

			}
		} else {
			if (tipos.get(i).contains("List")) {
				output.write("new ArrayList<>();");
				output.newLine();
				output.write("//TODO añadir objeto a colección");
				output.newLine();
			} else if (tipos.get(i).contains("Map")) {
				output.write("new HashMap<>();");
				output.newLine();
				output.write("//TODO añadir objeto a colección");
				output.newLine();
			} else if (tipos.get(i).contains("Integer")) {
				output.write("1;");

			} else if (tipos.get(i).contains("Long")) {
				output.write("1l;");

			} else if (tipos.get(i).contains("Boolean")) {
				output.write("true;");

			} else if (tipos.get(i).contains("Double")) {
				output.write("(double)1;");

			} else if (tipos.get(i).contains("String")) {
				output.write("\"1\";");

			} else {
				output.write("new " + tipos.get(i) + "();");
				output.newLine();
				output.write("//TODO inicializar objeto");
				output.newLine();
			}
		}

	}

	private static Object[] procesarFichero(FileReader fr, BufferedReader br) throws IOException {
		String linea;
		String clase = null;
		boolean claseB = false;
		StringBuilder atributo = new StringBuilder();
		StringBuilder tipo = new StringBuilder();
		ArrayList<String> atributos = new ArrayList<>();
		ArrayList<String> tipos = new ArrayList<>();
		boolean salir = false;
		int cont = 0;
		Object[] objects = new Object[3];

		while ((linea = br.readLine()) != null) {
			linea = linea.trim();
			if (linea.contains("class") && linea.contains("{") && !claseB && !linea.contains("final")) {
				int antes = linea.indexOf("class") + 6;
				clase = linea.substring(antes);
				System.out.println(clase);
				clase = clase.substring(0, clase.indexOf(" "));
				System.out.println(clase);
				claseB = true;
			}
			if (linea.contains("private") && linea.contains(";") && !linea.contains("final")) {
				for (int i = linea.length() - 2; i > 0 && !salir; i--) {
					if (linea.charAt(i) == ' ') {
						salir = true;
					} else {
						atributo.append(linea.charAt(i));
						cont++;
					}

				}
				salir = false;
				atributos.add(atributo.reverse().toString().trim());
				atributo = new StringBuilder();

				for (int i = linea.length() - (3 + cont); i > 0 && !salir; i--) {
					if (linea.charAt(i) == ' ' && linea.charAt(i - 1) != ',') {
						salir = true;

					} else {
						tipo.append(linea.charAt(i));
					}

				}
				salir = false;
				tipos.add(tipo.reverse().toString().trim());
				tipo = new StringBuilder();
				cont = 0;
			}

		}

		if (clase != null) {

			StringTokenizer stringTokenizer = new StringTokenizer(clase);

			objects[0] = stringTokenizer.nextToken();
			objects[1] = atributos;
			objects[2] = tipos;
		}
		System.out.println("Clase: " + objects[0]);
		System.out.println("Atributos: " + objects[1]);
		System.out.println("Tipos: " + objects[2]);

		return objects;
	}

}
