package Principal;

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
    public OpcItem tagSP, tagPV, tagMV, tagModo;
    public OpcGroup grupo, resposta;
    public double SP, PV, MV, Modo;
    public Date data;
    public boolean leitura = false;
    public TimeSeries serieSP, seriePV, serieMV;
    public TimeSeriesCollection dataset;
    public ChartPanel myChartPanel;
    public JFreeChart grafico;
    public int cont,k, ciclo;
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

        grupo = new OpcGroup("grupo1", true, 500, 0.0f);

        grupo.addItem(tagSP);
        grupo.addItem(tagPV);
        grupo.addItem(tagMV);
        grupo.addItem(tagModo);

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

    public void lerTags() {
        data = new Date();

        try {
            resposta = opc.synchReadGroup(grupo);
        } catch (ComponentNotFoundException | SynchReadException ex) {
            JOptionPane.showMessageDialog(null, "Erro na Leitura das Tags!!!");
            Logger.getLogger(OPCRead.class.getName()).log(Level.SEVERE, null, ex);
        }
        SP = Double.parseDouble(resposta.getItems().get(0).getValue().toString());
        PV = Double.parseDouble(resposta.getItems().get(1).getValue().toString());
        MV = Double.parseDouble(resposta.getItems().get(2).getValue().toString());
        Modo = Double.parseDouble(resposta.getItems().get(3).getValue().toString());

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
            
        
    }

    public void loopLeitura() {

        Thread ThreadOpc = null;
        ThreadOpc = new Thread() {
            public void run() {
                while (leitura) {

                    try {
                        lerTags();
                        esperar(Long.parseLong(cbAmostragem.getSelectedItem().toString()));
                    } catch (InterruptedException ex) {
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
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        panelGrafico = new javax.swing.JPanel();

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
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSPout))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPVout))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMVout))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnIniciarLeitura, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnModo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addComponent(txtMVout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        panelGrafico.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout panelGraficoLayout = new javax.swing.GroupLayout(panelGrafico);
        panelGrafico.setLayout(panelGraficoLayout);
        panelGraficoLayout.setHorizontalGroup(
            panelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 449, Short.MAX_VALUE)
        );
        panelGraficoLayout.setVerticalGroup(
            panelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGrafico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(100, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelGrafico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    private javax.swing.JComboBox<String> cbAmostragem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelGrafico;
    private javax.swing.JTextField txtHost;
    private javax.swing.JTextField txtMV;
    private javax.swing.JTextField txtMVout;
    private javax.swing.JTextField txtModo;
    private javax.swing.JTextField txtPV;
    private javax.swing.JTextField txtPVout;
    private javax.swing.JTextField txtSP;
    private javax.swing.JTextField txtSPout;
    private javax.swing.JTextField txtServidor;
    // End of variables declaration//GEN-END:variables
}
