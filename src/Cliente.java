package src.codigoNovo;
import java.net.*;
import java.io.*;
// Connectar ao porto 6500 de um servidor especifico,
// envia uma mensagem e imprime resultado,

public class Cliente {
    static boolean conexao = true;
    // usage: java EchoClient <servidor> <mensagem>
    public static void main(String args[]) throws Exception {


        ClienteUDP clienteUDP = new ClienteUDP();
        Socket socket = new Socket("25.75.211.87", 6500);
        clienteUDP.start();
        BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
        String confirmacaoLogin = "login " + socket.getLocalAddress().toString().substring(1);
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ps.println(confirmacaoLogin);

        menuInicial();

        while (conexao) {

            String mensagem = br2.readLine();

            if (mensagem.equals("0")) {
                menuInicial();
            } else if(mensagem.equals("2")) {

                System.out.print("Utilizador? ");
                mensagem += " " + br2.readLine();
                System.out.print("Mensagem? ");
                mensagem += " " + br2.readLine();
                ps = new PrintStream(socket.getOutputStream());
                ps.println(mensagem);

            } else if (mensagem.equals("3")) {

                System.out.print("Mensagem? ");
                mensagem += " " + br2.readLine();
                ps = new PrintStream(socket.getOutputStream());
                ps.println(mensagem);

            } else if (mensagem.equals("99")) {

                ps = new PrintStream(socket.getOutputStream());
                ps.println("99");
                System.out.println("A Sair");
                conexao = false;
                System.exit(0);

            } else {

                ps = new PrintStream(socket.getOutputStream());
                ps.println(mensagem); // escreve mensagem na socket
            }

        }
        clienteUDP.ligado = false;
        socket.close(); // termina socket
        System.out.println("Cliente Desconectado");
    }

    static void menuInicial(){
        System.out.println("0  - Menu Inicial");
        System.out.println("1  - Listar utilizadores online");
        System.out.println("2  - Enviar mensagem a um utilizador");
        System.out.println("3  - Enviar mensagem a todos os utilizador");
        System.out.println("4  - lista branca de utilizadores");
        System.out.println("5  - lista negra de utilizadores");
        System.out.println("99 - Sair");
    }
}

class ClienteUDP extends Thread {
    private  DatagramSocket socket;
    public boolean ligado;
    private byte[] buffer = new byte[256];

    public ClienteUDP() {
        try {//nova alteraçao
            socket = new DatagramSocket(9031);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run(){

        ligado = true;//nova alteraçao
        while(ligado){
            DatagramPacket pacote = new DatagramPacket(buffer,buffer.length);

            try {
                socket.receive(pacote);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String received = new String(pacote.getData(), 0, pacote.getLength());

            if (received.equals("IP inválido")) {

                System.err.println("O seu IP está na blacklist");
                System.exit(0);
                break;

            }else if(received.equals("IP não está presente no servidor")){
                System.err.println("IP não está presente no servidor");
                System.exit(0);
                break;
            }

            System.out.println(received);
        }
        socket.close();
    }
}
