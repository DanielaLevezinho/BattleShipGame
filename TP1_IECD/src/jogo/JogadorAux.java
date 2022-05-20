package jogo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JogadorAux {
	private final static String DEFAULT_HOST = "localhost";
	private final static int DEFAULT_PORT = 80;
	Scanner scan = new Scanner(System.in);

	private Socket clientSocket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private boolean emJogo = false;
	private String nickname;
	private boolean leaveStart;
	private static String protocoloXSD = "C:\\Users\\letic\\OneDrive\\Ambiente de Trabalho\\isel\\sem iv\\iecd\\TP1_IECD\\TP1_IECD\\WebContent\\xml\\xsdProtocolo.xsd";

	public JogadorAux() {
		try {
			this.clientSocket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			start();
		} catch (IOException e) {
			System.err.println("Erro na liga��o: " + e.getMessage());
		}
	}
	
	public void start() {
		while (!leaveStart) {
			System.out.println("Bem vindo!");
			System.out.println("1 - Efetuar Login;");
			System.out.println("2 - Efetuar Registo;");
			System.out.println("3 - Sair;\n");
			System.out.println("Insira a op��o: ");
			String opcao = scan.nextLine();
			switch (opcao) {
			case "1":
				System.out.println("Nickname: ");
				this.nickname = scan.nextLine();
				System.out.println("Password: ");
				String password = scan.nextLine();
				out.println(formularPedido("validar", "<password>" + password + "</password>"));
				try {
					processarResposta(in.readLine());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case "2":
				System.out.println("Nickname: ");
				this.nickname = scan.nextLine();
				System.out.println("Password: ");
				String novaPassword = scan.nextLine();
				out.println(formularPedido("registar", "<password>" + novaPassword + "</password>"));
				try {
					processarResposta(in.readLine());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case "3":
				leaveStart = true;
				System.out.println("At� breve!\n");
				try {
					clientSocket.close();
					in.close();
					out.close();
					scan.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
				break;
			default:
				System.out.println("Op��o inv�lida!");
			}
		}
		menuPrincipal();
	}
	
	private void menuPrincipal() {
		for (;;) {
			System.out.println("Bem vindo!");
			System.out.println("1 - Jogar;");
			System.out.println("2 - Mudar foto de perfil;");
			System.out.println("3 - Ver ranking;");
			System.out.println("4 - Sair;\n");
			System.out.println("Insira a op��o: ");
			String opcao = scan.nextLine();
			switch (opcao) {
			case "1":
				this.out.println(formularPedido("jogar", ""));
				try {
					processarResposta(in.readLine());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case "2":
				// Pedido mudar foto
				break;
			case "3":
				this.out.println(formularPedido("ranking", ""));
				try {
					processarResposta(in.readLine());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case "4":
				this.out.println(formularPedido("logout", ""));
				try {
					processarResposta(in.readLine());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			default:
				System.out.println("Op��o inv�lida!");
			}
		}
	}
	
	private void jogo() {
		emJogo = true;
		while (emJogo) {
			try {
				this.out.println(formularPedido("apresentar", ""));
				String lido = this.in.readLine();
				processarResposta(lido);
				System.out.println(this.in.readLine() + "\n"); // "Aguardar turno"
				if (!emJogo) break;

				// Pedir tabuleiro atualizado
				this.out.println(formularPedido("apresentar", ""));
				lido = this.in.readLine();
				processarResposta(lido);
				System.out.println(this.in.readLine()); // "Inserir Jogada"

				// Jogar
				String jogada = scan.nextLine();
				while(formularPedido("disparar", "<tiro>" + jogada + "</tiro>") == null) {
					jogada = scan.nextLine();
				}
				this.out.println(formularPedido("disparar", "<tiro>" + jogada + "</tiro>"));
				lido = this.in.readLine();
				if (lido == null)
					break;
				processarResposta(lido);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			scan.close();
			in.close();
		} catch (IOException e) {
		}
	}
	
	private void respostaLogout(Element reply) {
		System.out.println(reply.getTextContent() + "\n");
		try {
			scan.close();
			out.close();
			in.close();
		} catch (IOException e) {
		}
		System.exit(0);
	}
	
	private void jogar(Element reply) {
		System.out.println(reply.getTextContent() + "\n");
		jogo();
	}
	
	private void ranking(Element reply) {
		System.out.println("\nRanking de Jogadores: ");
		NodeList jogadores = reply.getElementsByTagName("jogadorRanking");
		for (int i = 0; i < jogadores.getLength() ; i++) {
			System.out.println("\t" + jogadores.item(i).getAttributes().getNamedItem("nome").getNodeValue() + " - " + 
							   jogadores.item(i).getAttributes().getNamedItem("vitorias").getNodeValue() + " vit�rias.");
		}
		System.out.println("\n");
	}
	
	private void apresentar(Element reply) {
		NodeList barcos = reply.getElementsByTagName("barco");
		NodeList tirosSofridos = reply.getElementsByTagName("tiroSofrido");
		NodeList tirosDados = reply.getElementsByTagName("tiroDado");
		Map<String, String> casasJogador = new HashMap<String, String>();
		Map<String, String> casasOponente = new HashMap<String, String>();

		for (int i = 0; i < tirosSofridos.getLength(); i++) {
			casasJogador.put(tirosSofridos.item(i).getAttributes().getNamedItem("posicao").getNodeValue(),
					tirosSofridos.item(i).getTextContent());
		}
		for (int i = 0; i < barcos.getLength(); i++) {
			NodeList posicoes = barcos.item(i).getChildNodes();
			for (int j = 0; j < posicoes.getLength(); j++) {
				if (casasJogador.containsKey(posicoes.item(j).getTextContent()))
					continue;
				casasJogador.put(posicoes.item(j).getTextContent(),
						barcos.item(i).getAttributes().getNamedItem("tipo").getNodeValue());
			}
		}
		for (int i = 0; i < tirosDados.getLength(); i++) {
			casasOponente.put(tirosDados.item(i).getAttributes().getNamedItem("posicao").getNodeValue(),
					tirosDados.item(i).getTextContent());
		}

		System.out.print("\t      " + "\"O meu Tabuleiro\"" + "\t\t\t\t\t     \"Tabuleiro Oponente\"\n");
		System.out.print("     1   2   3   4   5   6   7   8   9   10\t\t     1   2   3   4   5   6   7   8   9   10\n");
		System.out.print(
				"   -----------------------------------------\t\t   -----------------------------------------\n");
		for (int i = 0; i < 10; i++) {
			System.out.print(" " + (char) (i + 1 + 64) + " |");
			for (int j = 0; j < 10; j++) {
				String pos = (char) (i + 1 + 64) + String.valueOf(j + 1);
				if (casasJogador.containsKey(pos))
					System.out.print(" " + casasJogador.get(pos) + " ");
				else
					System.out.print("   ");
				System.out.print("|");
			}
			System.out.print("\t\t " + (char) (i + 1 + 64) + " |");
			for (int j = 0; j < 10; j++) {
				String pos = (char) (i + 1 + 64) + String.valueOf(j + 1);
				if (casasOponente.containsKey(pos))
					System.out.print(" " + casasOponente.get(pos) + " ");
				else
					System.out.print("   ");
				System.out.print("|");
			}

			System.out.print("\n   -----------------------------------------\t\t   -----------------------------------------\n");
		}
		System.out.print("\n");
	}
	
	private void registar(Element reply) {
		System.out.println(reply.getTextContent() + "\n");
		if (reply.getTextContent().equals("Conta criada!")) leaveStart = true;
	}
	
	private void validar(Element reply) {
		System.out.println(reply.getTextContent() + "\n");
		if (reply.getTextContent().equals("Login validado!")) leaveStart = true;
	}

	private void disparar(Element reply) {
		System.out.println(reply.getTextContent() + "\n");
		if (reply.getTextContent().equals("O jogador venceu!!!")) emJogo = false;
	}
	
	public static final boolean validarPedido(String pedido) throws SAXException {
		Document doc = null;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(pedido));
			doc = db.parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		} 
		
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new File(protocoloXSD));
		Validator validator = schema.newValidator();
		
		try {
			validator.validate(new DOMSource(doc));
			return true;
		} catch (IOException | SAXException e) {
			return false;
		}
	}
	
	private String formularPedido(String nomeMetodo, String conteudo) {
		String pedidoXML = "<protocolo><" + nomeMetodo + "><pedido><nickname>" + nickname + "</nickname>" + conteudo +  "</pedido></" + nomeMetodo + "></protocolo>";
//		try {
//			if (!validarPedido(pedidoXML)) {
//				System.out.println("Inv�lido! Introduza outro valor: ");
//				return null;
//			};
//		} catch (SAXException e) {
//			return null;
//		}
		return pedidoXML; 
	}

	private void processarResposta(String pedido) {
		Document doc = null;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(pedido));
			doc = db.parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		Node protocolo = doc.getElementsByTagName("protocolo").item(0);
		Node metodo = protocolo.getFirstChild();
		String metodoNome = metodo.getNodeName();
		Node reply = metodo.getLastChild();

		switch (metodoNome) {
		case "validar":
			validar((Element) reply);
			break;
		case "registar":
			registar((Element) reply);
			break;
		case "jogar":
			jogar((Element) reply);
			break;
		case "logout":
			respostaLogout((Element) reply);
			break;
		case "ranking":
			ranking((Element) reply);
			break;
		case "apresentar":
			apresentar((Element) reply);
			break;
		case "disparar":
			disparar((Element) reply);
			break;
		}
	}

	public static void main(String[] args) {
		new Jogador();
	}
}