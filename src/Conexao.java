package src.codigoNovo;

import java.io.*;
import java.net.*;
import java.util.*;

public class Conexao extends Thread {
    //TCP
    private final Socket clientSocket;
    private final Servidor server;
    private String login = null;

    //UDP
    private DatagramSocket datagramSocket;
    private InetAddress endereco;
    private byte[] buffer;

    public Conexao(Servidor server, Socket clientSocket, InetAddress ip) throws SocketException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.endereco = ip;
        this.login = ip.toString().substring(1);
        this.datagramSocket = new DatagramSocket();
    }

    @Override
    public void run() {
        try {
            runClientSocket();
        } catch (IOException | InterruptedException e) {
            if (e.getMessage().equalsIgnoreCase("Connection reset")) {
                try {
                    handlelogoff();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                datagramSocket.close();
            } else {
                e.printStackTrace();
            }
        }
    }

    private void runClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        List<Conexao> conexaoList = server.getConexaoList();
        String msg;
        boolean loggedIn = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        line = reader.readLine();

        String[] tokensLogin = line.split(" "); // login + IP

        if(listaNegra().contains(tokensLogin[1])){//---------nao entra
            loggedIn = false;
            send("IP inválido");
            System.err.println("IP " + tokensLogin[1] + " inválido");
            handlelogoff();
            datagramSocket.close();

        }else if(listaBranca().contains(tokensLogin[1])){//------entra
            loggedIn = true;
            send("Conectado");

        }else if (listaBranca().size() == 0) {//-----------------entra
            loggedIn = true;
            send("Conectado");

        }else{//---------------------------------------------nao entra
            loggedIn = false;
            send("IP não está presente no servidor");
            System.err.println("IP " + tokensLogin[1] + " inválido");
            handlelogoff();
            datagramSocket.close();

        }

        if(loggedIn){
            while (loggedIn && (line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                msg = "";
                System.out.println("Pedido de " + login + " : " + line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    switch(cmd){

                        case "1": //listarUsers------------------------------------------
                            listarUsuariosOnline();
                            break;

                        case "2": //mensagemPrivada--------------------------------------
                            String[] tokenMensagem = line.split(" ", 3);
                            mensagemPrivada(tokenMensagem);
                            break;

                        case "3": //mensagemGeral----------------------------------------
                            String[] tokenMensagemGeral = line.split(" ", 2);
                            mensagemGeral(tokenMensagemGeral);
                            break;

                        case "4": //listaBranca------------------------------------------
                            ArrayList<String> listaBranca = listaBranca();
                            conexaoList = server.getConexaoList();
                            msg = "Lista branca:\n";

                            for (String ipList : listaBranca){
                                msg += ipList + "\n";
                            }

                            for(Conexao conexao: conexaoList){
                                if (login.equals(conexao.getLogin())) {
                                    conexao.send(msg);
                                }
                            }
                            break;

                        case "5": //listaNegra-----------------------------------------
                            ArrayList<String> listaNegra= listaNegra();
                            conexaoList = server.getConexaoList();
                            msg = "Lista negra:\n";

                            for (String ipList : listaNegra){
                                msg += ipList + "\n";
                            }

                            for(Conexao conexao: conexaoList){
                                if (login.equals(conexao.getLogin())) {
                                    conexao.send(msg);
                                }
                            }
                            break;

                        case "99"://logout---------------------------------------------
                            handlelogoff();
                            loggedIn = false;
                            break;

                        default:
                            send("Opção não existente.");
                            System.err.println("Pedido não reconhecido.");
                    }
                }
            }
        }

    }

    private void listarUsuariosOnline(){

        List<Conexao> conexaoList = server.getConexaoList();
        String usersOnline = "";
        int contador = 0;
        for(Conexao conexao: conexaoList){
            usersOnline += contador + " - " + conexao.getLogin() + "\n";
            contador++;
        }

        try {
            send(usersOnline);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // formato: "msgPrivate" "login" msg...
    private void mensagemPrivada(String[] tokens) throws IOException {
        List<Conexao> conexaoList = server.getConexaoList();

        if (conexaoList.size() < Integer.parseInt(tokens[1])){

            send("O utilizador não existe\n");

        } else {

            Conexao recetor = conexaoList.get(Integer.parseInt(tokens[1])); //Para quem envia

            String ipRecetor = recetor.getLogin();
            String msg = tokens[2]; //Mensagem em si

            for (Conexao conexao : conexaoList ) {
                if (ipRecetor.equalsIgnoreCase(conexao.getLogin())) {
                    String outMsg = "Mensagem de " + login + " -> " + msg + "\n";
                    conexao.send(outMsg);
                    send("OK, enviado para " + ipRecetor + "\n");
                }
            }
        }
    }

    private void mensagemGeral(String[] tokens) throws IOException {

        String msg = tokens[1];

        List<Conexao> conexaoList = server.getConexaoList();
        for (Conexao conexao : conexaoList ) {
            if(login.equals(conexao.getLogin())){
                send("OK, mensagem enviada a todos os utilizadores\n");
            }else{
                String outMsg = "Mensagem de " + login + " -> " + msg + "\n";
                conexao.send(outMsg);
            }
        }
    }

    private void handlelogoff() throws IOException {

        System.out.println("O IP " + login + " foi desconectado.");
        server.removeConexao(this); //tira da lista
        List<Conexao> conexaoList = server.getConexaoList();
        clientSocket.close();
    }

    public String getLogin() {


        return login;
    }



    private void send(String msg) throws IOException{

        if (login != null) { //login é o nome do utilizador ou IP

            buffer = msg.getBytes();
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, endereco, 9031);
            datagramSocket.send(pacote);

        }
    }


    private ArrayList<String> listaBranca(){//todo ler ficheiro txt e imprimir users na white list
        ArrayList<String> listaBrancaCompleta = lerLista("listaBranca.txt");
        return listaBrancaCompleta;
    }

    private ArrayList<String> listaNegra(){//todo ler ficheiro txt e imprimir users na black list
        ArrayList<String> listaNegraCompleta = lerLista("listaNegra.txt");
        return listaNegraCompleta;
    }

    static ArrayList<String> lerLista(String filename){

        ArrayList<String> listaCompleta = new ArrayList<>();
        try {
            File listaTxt = new File("listas/" + filename);
            Scanner myReader = new Scanner(listaTxt);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                listaCompleta.add(data);
            }

            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro não encontrado");
            e.printStackTrace();
        }

        return listaCompleta;
    }

}