package Principal;

import Jama.Matrix;
import java.awt.Color;
import static java.lang.Thread.sleep;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafish.clients.opc.JCustomOpc;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.ComponentNotFoundException;
import javafish.clients.opc.exception.ConnectivityException;
import javafish.clients.opc.exception.SynchReadException;
import javafish.clients.opc.exception.SynchWriteException;
import javafish.clients.opc.exception.UnableAddGroupException;
import javafish.clients.opc.exception.UnableAddItemException;
import javafish.clients.opc.exception.UnableRemoveGroupException;
import javafish.clients.opc.variant.Variant;
import javax.swing.JOptionPane;
import javax.swing.Spring;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class OPCRead extends javax.swing.JFrame {

    public JOpc opc;
    public OpcItem tagSP, tagPV, tagMV, tagModo, tagKp, tagKi, tagKd;
    public OpcGroup grupo, resposta;
    public double SP, PV, MV, Modo;
    public Date data;
    public boolean leitura = false, flagRele=false;
    public TimeSeries serieSP, seriePV, serieMV;
    public TimeSeriesCollection dataset;
    public ChartPanel myChartPanel;
    public JFreeChart grafico;
    public int cont,k, ciclo=8;
    public ArrayList<Integer> periodos = new ArrayList<>();
    public ArrayList<Double> y=new ArrayList<>();
    public ArrayList<Double> u=new ArrayList<>();
    public double erro, eps, amp, mvInicio;
    
    

    public OPCRead() {
        initComponents();
        iniciarGrafico();
    }

    public void iniciarGrafico() {
        serieSP = new TimeSeries("SP");
        seriePV = new TimeSeries("PV");
        serieMV = new TimeSeries("MV");

        dataset = new TimeSeriesCollection();

        dataset.addSeries(serieSP);
        dataset.addSeries(seriePV);
        dataset.addSeries(serieMV);

        XYDataset dados = dataset;
        grafico = ChartFactory.createTimeSeriesChart("Dados da Planta", "Tempo", "Valores", dados);
        myChartPanel = new ChartPanel(grafico, true);
        myChartPanel.setSize(panelGrafico.getWidth(), panelGrafico.getHeight());
        myChartPanel.setVisible(true);

        panelGrafico.removeAll();
        panelGrafico.add(myChartPanel);
        panelGrafico.revalidate();
        panelGrafico.repaint();

    }

    public void conectarOPC() throws UnableAddGroupException, UnableAddItemException {
        JOpc.coInitialize();

        opc = new JOpc(txtHost.getText(), txtServidor.getText(), "opc");
        tagSP = new OpcItem(txtSP.getText(), true, "");
        tagPV = new OpcItem(txtPV.getText(), true, "");
        tagMV = new OpcItem(txtMV.getText(), true, "");
        tagModo = new OpcItem(txtModo.getText(), true, "");
        tagKp=new OpcItem("tagKp", true, "");
        tagKi=new OpcItem("tagKi", true, "");
        tagKd=new OpcItem("tagKd", true, "");
        //tagKp=new OpcItem("[P_UNP]N7:16", true, "");
        //tagKi=new OpcItem("[P_UNP]N7:17", true, "");
        //tagKd=new OpcItem("[P_UNP]N7:18", true, "");

        grupo = new OpcGroup("grupo1", true, 500, 0.0f);

        grupo.addItem(tagSP);
        grupo.addItem(tagPV);
        grupo.addItem(tagMV);
        grupo.addItem(tagModo);
        grupo.addItem(tagKp);
        grupo.addItem(tagKi);
        grupo.addItem(tagKd);
        
        //kp=n7:16(0-10000)
        //ki=n7:17(0-10000)
        //kd=n7:18(0-10000)

        opc.addGroup(grupo);

        try {
            opc.connect();
            opc.registerGroups();
            LabelStatus.setText("Conectado!");
            LabelStatus.setForeground(Color.blue);
        } catch (ConnectivityException ex) {
            LabelStatus.setText("Falha Conexão");
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public void desconectarOPC() {
        try {
            opc.unregisterGroups();
        } catch (UnableRemoveGroupException ex) {
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOpc.coUninitialize();
    }

    public void lerTags() throws InterruptedException, ComponentNotFoundException, SynchWriteException {
        data = new Date();

        try {
            resposta = opc.synchReadGroup(grupo);
        } catch (ComponentNotFoundException | SynchReadException ex) {
            JOptionPane.showMessageDialog(null, "Erro na Leitura das Tags!!!");
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }
        //SP = Double.parseDouble(resposta.getItems().get(0).getValue().toString());
        //PV = Double.parseDouble(resposta.getItems().get(1).getValue().toString());
        //MV = Double.parseDouble(resposta.getItems().get(2).getValue().toString());
        SP = (Double.parseDouble(resposta.getItems().get(0).getValue().toString()))/16333*100;
        PV = (Double.parseDouble(resposta.getItems().get(1).getValue().toString()))/16333*100;
        MV = (Double.parseDouble(resposta.getItems().get(2).getValue().toString()))/16333*100;
        Modo = Double.parseDouble(resposta.getItems().get(3).getValue().toString());

        txtKpOut.setText(resposta.getItems().get(4).getValue().toString());
        txtKiOut.setText(resposta.getItems().get(5).getValue().toString());
        txtKdOut.setText(resposta.getItems().get(6).getValue().toString());
        txtSPout.setText(truncar(SP));
        txtPVout.setText(truncar(PV));
        txtMVout.setText(truncar(MV));
        //     txtSPout.setText(resposta.getItems().get(0).getValue().toString());
        //     txtPVout.setText(resposta.getItems().get(1).getValue().toString());
        //     txtMVout.setText(resposta.getItems().get(2).getValue().toString());
        //Atualizar Gráfico
        serieSP.addOrUpdate(new Millisecond(data), SP);
        seriePV.addOrUpdate(new Millisecond(data), PV);
        serieMV.addOrUpdate(new Millisecond(data), MV);
        
        if(Modo > 0 ){
            btnModo.setBackground(Color.red);
            btnModo.setText("Manual");
        }  else{
            btnModo.setBackground(Color.green);
            btnModo.setText("Automático");
        }
        //iniciar rele
        if (flagRele) {
            if (cont<ciclo) {
                rele();
            } else {
                pararRele();
            }
        }
            
        
    }

    public void loopLeitura() {

        Thread ThreadOpc = null;
        ThreadOpc = new Thread() {
            public void run() {
                while (leitura) {
                    try {
                        lerTags();
                        esperar(Long.parseLong(cbAmostragem.getSelectedItem().toString()));
                    } catch (InterruptedException | ComponentNotFoundException | SynchWriteException ex) {
                        Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

        };
        ThreadOpc.start();

    }

    public void esperar(Long tempo) throws InterruptedException {
        if (tempo > 0) {
            sleep(tempo);
        }
    }

    public static String truncar(double valor) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(valor);

    }

    public void escrever(OpcItem tag, double valor) {
        tag.setValue(new Variant(valor));
        try {
            opc.synchWriteItem(grupo, tag);
        } catch (ComponentNotFoundException | SynchWriteException ex) {
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void rele(){
        erro=SP-PV;
        if(cont==0){
            if (erro>=0) {
                escrever(tagMV,mvInicio+amp);
            } else {
                escrever(tagMV,mvInicio-amp);
            }
        }else if(erro>=eps){
            escrever(tagMV,mvInicio+amp);
        }else if(erro<=(-eps)){
            escrever(tagMV,mvInicio-amp);
        }
        y.add(PV);
        u.add(MV);
        
        if(k>1){
            if(Math.round(u.get(k))!=Math.round(u.get(k-1))){
                cont++;
                periodos.add(k);
            }
        }
        k++;
    }
    //novas funções
    
    public void iniciarRele() throws ComponentNotFoundException, SynchReadException, InterruptedException, SynchWriteException {
        /*colocar o controlador em manual*/
        escrever(tagModo, 1);
        btnModo.setText("Manual");
        esperar((long) 20);
        k = 0;
        cont = 0;
        /*lê a posição atual do sinal de controle */
        mvInicio = MV * 16383 / 100;
        /*Pegar a amplitude do relé e histerese*/
        amp = Double.parseDouble(txt_AmpRele.getText()) * 16383 / 100;
        eps = Double.parseDouble(txt_Eps.getText());
        /*escrever o sinal de controle com degrau*/
        flagRele = true;
        btn_ParaRele.setEnabled(true);
        btn_IniciarRele.setEnabled(false);
    }

    public void pararRele() throws InterruptedException, ComponentNotFoundException, SynchWriteException {

        escrever(tagModo, 0);
        esperar((long) 20);
        flagRele = false;
        y.clear();
        u.clear();
        btn_ParaRele.setEnabled(false);
        btn_IniciarRele.setEnabled(true);


    }

    public static ArrayList<Double> ModFOPDT(ArrayList<Integer> periodos, double amp, double eps, double ref, ArrayList<Double> y, ArrayList<Double> u, double Tamostragem) {
        ArrayList<Double> ParFOPDT = new ArrayList<>();
        double Tu, RefAux, Au, Ad, a, Ku, fase, k, tau, teta;
        int aux1, aux2;
        ParFOPDT.clear();

        //calculo do periodo total
        Tu = ((periodos.get(5) - periodos.get(4)) * Tamostragem) + ((periodos.get(4) - periodos.get(3)) * Tamostragem);
        aux1 = periodos.get(2);
        aux2 = periodos.get(4);
        double yi1[] = new double[aux2 - aux1 + 2];
        double yi2[] = new double[aux2 - aux1 + 2];
        double ui1[] = new double[aux2 - aux1 + 2];
        double ui2[] = new double[aux2 - aux1 + 2];
        double ti1[] = new double[aux2 - aux1 + 2];
        double ti2[] = new double[aux2 - aux1 + 2];

        RefAux = ref;
        for (int t = aux1; t <= aux2 - 1; t++)//pico de positivo
        {
            if (y.get(t - 1) >= RefAux) {
                RefAux = y.get(t - 1);
            }
        }
        Au = RefAux;//guardar pico de subida
        RefAux = ref;

        for (int t = aux1; t <= aux2 - 1; t++)//pico negativo
        {
            if (y.get(t - 1) <= RefAux) {
                RefAux = y.get(t - 1);
            }
        }
        Ad = RefAux;

        a = (Math.abs(Au) - Math.abs(Ad)) / 2;//amplitude de saída
        //função descritiva do relé
        Ku = (4 * amp) / (Math.PI * a);
        fase = Math.asin((eps / a)) * -1;//calcular defasagem da histerese
        //-----<Inicio do Calculo Ganho estático - Méetodo da Integral>---------------- 
        int i = 0;
        yi1[i] = 0;
        ui1[i] = 0;
        ti2[i] = 0;
        for (int t = aux1; t <= aux2; t++) {//laço para colher os dados de 1 periodos completo do teste
            yi1[i + 1] = y.get(t - 1);
            yi2[i] = y.get(t - 1);

            ui1[i + 1] = u.get(t - 1);
            ui2[i] = u.get(t - 1);

            ti1[i] = (i + 1) * Tamostragem;
            ti2[i + 1] = (i + 1) * Tamostragem;
            i = i + 1;
        }
        yi2[i] = 0;
        ui2[i] = 0;
        ti1[i] = 0;

        Matrix Yi1 = new Matrix(yi1, 1);
        Matrix Yi2 = new Matrix(yi2, 1);
        Matrix Ti1 = new Matrix(ti1, 1);
        Matrix Ti2 = new Matrix(ti2, 1);

        Yi1 = Yi1.plusEquals(Yi2);
        Ti1 = Ti1.minusEquals(Ti2);
        Yi1 = Yi1.arrayTimes(Ti1).times(0.5);

        double A1 = 0;
        for (int j = 1; j < Yi1.getColumnDimension() - 1; j++) {
            A1 = A1 + Yi1.get(0, j);
        }
        Matrix Ui1 = new Matrix(ui1, 1);
        Matrix Ui2 = new Matrix(ui2, 1);

        Ui1 = Ui1.plusEquals(Ui2);
        Ui1 = Ui1.arrayTimes(Ti1).times(0.5);
        //Ui1 = Ui1.times(0.5);

        double A2 = 0;
        for (int j = 1; j < Ui1.getColumnDimension() - 1; j++) {
            A2 = A2 + Ui1.get(0, j);
        }

        k = A1 / A2;//ganho estático
        double delta;
        if (Ku * k < 1) {
            delta = 1;
        } else {
            delta = Math.pow((Ku * k), 2) - 1;
        }

        //----<Fim do calculo ganho estático da planta>-------------------------------
        tau = (Tu / (2 * Math.PI)) * Math.sqrt(delta);
        teta = (Tu / (2 * Math.PI)) * (Math.PI - Math.atan((2 * Math.PI * tau) / Tu) + fase);

        ParFOPDT.add(0, k);
        ParFOPDT.add(1, tau);
        ParFOPDT.add(2, teta);

        return ParFOPDT;
    }
	
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtServidor = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        btnConectar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        LabelStatus = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSP = new javax.swing.JTextField();
        txtPV = new javax.swing.JTextField();
        txtMV = new javax.swing.JTextField();
        txtModo = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cbAmostragem = new javax.swing.JComboBox<>();
        btnIniciarLeitura = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtSPout = new javax.swing.JTextField();
        txtPVout = new javax.swing.JTextField();
        txtMVout = new javax.swing.JTextField();
        btnModo = new javax.swing.JToggleButton();
        jLabel24 = new javax.swing.JLabel();
        txtKpOut = new javax.swing.JTextField();
        txtKiOut = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtKdOut = new javax.swing.JTextField();
        panelGrafico = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        txt_AmpRele = new javax.swing.JTextField();
        jRadioButton2 = new javax.swing.JRadioButton();
        txt_Eps = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        cbo_metodo = new javax.swing.JComboBox<>();
        txt_K = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txt_Kp = new javax.swing.JTextField();
        txt_Tau = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txt_Ti = new javax.swing.JTextField();
        btn_IniciarRele = new javax.swing.JButton();
        txt_D = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        btn_ParaRele = new javax.swing.JButton();
        txt_Td = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Servidor OPC :");

        txtServidor.setText("ElipseSCADA.OPCSvr.1");
        txtServidor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServidorActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Host:");

        txtHost.setText("localhost");

        btnConectar.setText("Conectar");
        btnConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConectarActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Status:");

        LabelStatus.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        LabelStatus.setForeground(new java.awt.Color(255, 0, 0));
        LabelStatus.setText("NÃO CONECTADO");
        LabelStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LabelStatusActionPerformed(evt);
            }
        });

        jLabel4.setText("Tag SP :");

        jLabel5.setText("Tag PV :");

        jLabel6.setText("Tag MV :");

        jLabel7.setText("Tag Modo:");

        txtSP.setText("TagSP");
        txtSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSPActionPerformed(evt);
            }
        });

        txtPV.setText("TagPV");

        txtMV.setText("TagMV");

        txtModo.setText("TagModo");

        jLabel8.setText("Tempo de Amostragem :");

        cbAmostragem.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "100", "200", "500", "1000", "2000", "5000" }));
        cbAmostragem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAmostragemActionPerformed(evt);
            }
        });

        btnIniciarLeitura.setText("Iniciar Leitura");
        btnIniciarLeitura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarLeituraActionPerformed(evt);
            }
        });

        jLabel9.setText("SP:");

        jLabel10.setText("PV:");

        jLabel11.setText("MV:");

        txtSPout.setEditable(false);
        txtSPout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSPoutActionPerformed(evt);
            }
        });

        txtPVout.setEditable(false);

        txtMVout.setEditable(false);

        btnModo.setText("Automático");
        btnModo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModoActionPerformed(evt);
            }
        });

        jLabel24.setText("Kp:");

        txtKpOut.setEditable(false);
        txtKpOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKpOutActionPerformed(evt);
            }
        });

        txtKiOut.setEditable(false);

        jLabel25.setText("Ki:");

        jLabel26.setText("Kd:");

        txtKdOut.setEditable(false);
        txtKdOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKdOutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHost)
                    .addComponent(txtServidor)
                    .addComponent(btnConectar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LabelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSP)
                            .addComponent(txtPV)
                            .addComponent(txtMV)
                            .addComponent(txtModo)))
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbAmostragem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnIniciarLeitura, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtMVout))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtPVout))
                                .addComponent(jLabel2)
                                .addComponent(jLabel1)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel9)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtSPout, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnModo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel26)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtKdOut))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel24)
                                            .addComponent(jLabel25))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtKiOut, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtKpOut, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(11, 11, 11)
                .addComponent(txtServidor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnConectar)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(LabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtPV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtMV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtModo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbAmostragem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnIniciarLeitura)
                            .addComponent(btnModo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(txtSPout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtPVout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtMVout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(txtKpOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(txtKiOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(txtKdOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        panelGrafico.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout panelGraficoLayout = new javax.swing.GroupLayout(panelGrafico);
        panelGrafico.setLayout(panelGraficoLayout);
        panelGraficoLayout.setHorizontalGroup(
            panelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 411, Short.MAX_VALUE)
        );
        panelGraficoLayout.setVerticalGroup(
            panelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 517, Short.MAX_VALUE)
        );

        jLabel12.setText("Amplitude Relé (h)");

        jLabel13.setText("Histerese Relé (eps)");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("PI");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("PID");

        jLabel17.setText("Metodo");

        jLabel14.setText("K");

        cbo_metodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel18.setText("Kp");

        jLabel15.setText("Tau");

        jLabel19.setText("Ti");

        jLabel16.setText("D");

        btn_IniciarRele.setText("iniciar");
        btn_IniciarRele.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_IniciarReleActionPerformed(evt);
            }
        });

        jLabel20.setText("Td");

        btn_ParaRele.setText("parar");
        btn_ParaRele.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ParaReleActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel21.setText("METODO RELÉ");

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel22.setText("PARAMÊTROS ESTIMADOS");

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel23.setText("SINTONIA");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGrafico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 11, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(btn_IniciarRele)
                                .addGap(111, 111, 111)
                                .addComponent(btn_ParaRele))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addGap(18, 18, 18)
                                        .addComponent(txt_K))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel12)
                                        .addComponent(jLabel13))
                                    .addComponent(jRadioButton1))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(60, 60, 60)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txt_AmpRele)
                                            .addComponent(txt_Eps, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(53, 53, 53)
                                        .addComponent(jRadioButton2))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txt_Tau, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txt_D, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel18)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txt_Kp, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel19)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_Ti, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel20)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_Td))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel17)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbo_metodo, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addComponent(jSeparator2)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel22))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelGrafico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_IniciarRele)
                    .addComponent(btn_ParaRele))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txt_AmpRele, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txt_Eps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txt_K, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(txt_Tau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(txt_D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbo_metodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txt_Kp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(txt_Ti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(txt_Td, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtServidorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServidorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServidorActionPerformed

    private void LabelStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LabelStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LabelStatusActionPerformed

    private void txtSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSPActionPerformed

    private void cbAmostragemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAmostragemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbAmostragemActionPerformed

    private void txtSPoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSPoutActionPerformed

    }//GEN-LAST:event_txtSPoutActionPerformed

    private void btnConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConectarActionPerformed
        if (btnConectar.getText() == "Conectar") {

            btnConectar.setText("Desconectar");
            try {
                conectarOPC();
            } catch (UnableAddGroupException | UnableAddItemException ex) {
                Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            leitura=false;
            btnConectar.setText("Conectar");
            LabelStatus.setText("NÃO CONECTADO");
            LabelStatus.setForeground(Color.red);
            desconectarOPC();
        }
    }//GEN-LAST:event_btnConectarActionPerformed

    private void btnIniciarLeituraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarLeituraActionPerformed
        if (leitura) {
            leitura = false;
            btnIniciarLeitura.setText("Iniciar Leitura");

        } else {
            leitura = true;
            btnIniciarLeitura.setText("Parar Leitura");
            loopLeitura();
        }

    }//GEN-LAST:event_btnIniciarLeituraActionPerformed

    private void btnModoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModoActionPerformed
        if (btnModo.getText() == "Automático") {
            escrever(tagModo, 1);//Colocar em Manual
            btnModo.setText("Manual");
        } else {
            escrever(tagModo, 0);//Colocar em Autómatico
            btnModo.setText("Autómatico");
        }


    }//GEN-LAST:event_btnModoActionPerformed

    private void btn_IniciarReleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_IniciarReleActionPerformed
        try {
            iniciarRele();
        } catch (ComponentNotFoundException | SynchReadException | InterruptedException | SynchWriteException ex) {
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_IniciarReleActionPerformed

    private void btn_ParaReleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ParaReleActionPerformed
        try {
            pararRele();
        } catch (InterruptedException | ComponentNotFoundException | SynchWriteException ex) {
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_ParaReleActionPerformed

    private void txtKpOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKpOutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKpOutActionPerformed

    private void txtKdOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKdOutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKdOutActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OPCRead.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OPCRead.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OPCRead.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OPCRead.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OPCRead().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField LabelStatus;
    private javax.swing.JButton btnConectar;
    private javax.swing.JButton btnIniciarLeitura;
    private javax.swing.JToggleButton btnModo;
    private javax.swing.JButton btn_IniciarRele;
    private javax.swing.JButton btn_ParaRele;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbAmostragem;
    private javax.swing.JComboBox<String> cbo_metodo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel panelGrafico;
    private javax.swing.JTextField txtHost;
    private javax.swing.JTextField txtKdOut;
    private javax.swing.JTextField txtKiOut;
    private javax.swing.JTextField txtKpOut;
    private javax.swing.JTextField txtMV;
    private javax.swing.JTextField txtMVout;
    private javax.swing.JTextField txtModo;
    private javax.swing.JTextField txtPV;
    private javax.swing.JTextField txtPVout;
    private javax.swing.JTextField txtSP;
    private javax.swing.JTextField txtSPout;
    private javax.swing.JTextField txtServidor;
    private javax.swing.JTextField txt_AmpRele;
    private javax.swing.JTextField txt_D;
    private javax.swing.JTextField txt_Eps;
    private javax.swing.JTextField txt_K;
    private javax.swing.JTextField txt_Kp;
    private javax.swing.JTextField txt_Tau;
    private javax.swing.JTextField txt_Td;
    private javax.swing.JTextField txt_Ti;
    // End of variables declaration//GEN-END:variables
}
