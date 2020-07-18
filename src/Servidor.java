package src.codigoNovo;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Servidor {

    private static int portoServer;
    private static ArrayList<Conexao> conexaoList = new ArrayList<>();

    public Servidor(int portoServer) {
        this.portoServer = portoServer;
    }

    public List<Conexao> getConexaoList() {
        return conexaoList;
    }

    public void removeConexao(Conexao conexao) {

        conexaoList.remove(conexao);

    }

    public static void main(String[] args) throws IOException {
        int porto = 6500;
        Servidor server = new Servidor(porto);
        ServerSocket serverSocket = new ServerSocket(portoServer); //Aceitar conexões
        try {
            while (true) {
                System.out.println("Esperando uma nova conexão..");
                Socket clientSocket = serverSocket.accept(); //Conexão a espera de ser aceitada
                Conexao conexao = new Conexao(server, clientSocket, clientSocket.getInetAddress());
                System.out.println("Conexão aceite de " + conexao.getLogin());
                conexaoList.add(conexao);
                conexao.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERRO");
        }
    }
}