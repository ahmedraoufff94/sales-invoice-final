
package SIG.Controller;

import SIG.Model.InvoiceHeader;
import SIG.Model.InvoiceLine;
import SIG.Model.LineTableView;
import SIG.Model.TableView;
import SIG.View.InvDialog;
import SIG.View.LineDialog;
import SIG.View.SalesInvoiceFrame;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ActionHandler implements ActionListener, ListSelectionListener {

    private InvDialog invDialog;
    private LineDialog lineDialog;
    private SalesInvoiceFrame frame;
    private  int selectHeaderline = -1 ;
    public ActionHandler(SalesInvoiceFrame frame){
        this.frame=frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Action Handling called!");
        switch (e.getActionCommand()) {
            case "New Invoice":
                System.out.println("New invoice");
                newInv();
                break;

            case "Delete Invoice":
                System.out.println("Delete invoice");
                delInv();
                break;

            case "Add Item":
                System.out.println("Add Item");
                addItem();
                break;

            case "Delete Item":
                System.out.println("Delete Item");
                deleteItem();
                break;


            case "Load file":
                System.out.println("Load file");
                loadFile();
                break;

            case "Save file":
                System.out.println("Save file");
                saveFile();
                break;

            case "InvoiceCreated":
                System.out.println("InvoiceCreated");
                InvoiceCreated();
                break;

            case "NoInvoiceCreated":
                System.out.println("NoInvoiceCreated");
                NoInvoiceCreated();
                break;

            case "LineCreated":
                System.out.println("LineCreated");
                LineCreated();
                break;

            case "NoLineCreated":
                System.out.println("NoLineCreated");
                NoLineCreated();
                break;


            default:
                throw new AssertionError();
        }


    }

    private void newInv() {
        invDialog = new InvDialog(frame);
        invDialog.setVisible(true);

    }


    private void delInv() {
        int rowChoosen= frame.getHeaderTable().getSelectedRow();
        if (rowChoosen != -1){
            frame.getInvoicess().remove(rowChoosen);
            frame.getTableView().fireTableDataChanged();
        }
    }



    private void addItem() {
        lineDialog = new LineDialog(frame);
        lineDialog.setVisible(true);
    }

    private void deleteItem() {
        //int invoiceSelected = frame.getHeaderTable().getSelectedRow();
        int rowSelected = frame.getLineTable().getSelectedRow();

        if ( selectHeaderline > -1 && rowSelected != -1){
            InvoiceHeader invoiceHeader = frame.getInvoicess().get(selectHeaderline);
            invoiceHeader.getLines().remove(rowSelected);
            LineTableView lineTableView = new LineTableView(invoiceHeader.getLines());
            frame.getLineTable().setModel(lineTableView);

            lineTableView.fireTableDataChanged();
            frame.getTotalLabel().setText(""+invoiceHeader.getTotal_price());
            frame.getTableView().fireTableDataChanged();
        }
        else
        {
            System.out.println("please select Header row and invoice");
        }

    }


    private void saveFile() {
        ArrayList<InvoiceHeader> invoices = frame.getInvoicess();
        String invoiceHeader = "" ;
        String invoiceLines = "" ;
        for (InvoiceHeader invoiceHeaderss : invoices) {
            String csvInvoice = invoiceHeaderss.saveCsv();
            invoiceHeader = invoiceHeader + csvInvoice;
            invoiceHeader = invoiceHeader + "\n";

            for (InvoiceLine invoiceLine : invoiceHeaderss.getLines()) {
                String csvLine = invoiceLine.saveCsv();
                invoiceLines = invoiceLines + csvLine;
                invoiceLines = invoiceLines + "\n";
            }
        }
        try {
            JFileChooser fileChooser = new JFileChooser();
            int x = fileChooser.showSaveDialog(frame);
            if(x == JFileChooser.APPROVE_OPTION) {
                File invHeaderFile = fileChooser.getSelectedFile();
                FileWriter fileWriterHeader = new FileWriter(invHeaderFile);
                fileWriterHeader.write(invoiceHeader);
                fileWriterHeader.flush();
                fileWriterHeader.close();

                x = fileChooser.showSaveDialog(frame);
                if(x == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fileChooser.getSelectedFile();
                    FileWriter fileWriterLine = new FileWriter(lineFile);
                    fileWriterLine.write(invoiceLines);
                    fileWriterLine.flush();
                    fileWriterLine.close();
                }
            }
        }catch (Exception e) {
            // TODO: handle exception
        }
    }




    @Override
    public void valueChanged(ListSelectionEvent e) {
        int Index =frame.getHeaderTable().getSelectedRow();
        if (Index!= -1){
            selectHeaderline  =Index;
            InvoiceHeader selectedInvoice = frame.getInvoicess().get(Index);
            System.out.println("selection happened");

            frame.getNumLabel().setText(""+selectedInvoice.getNum());
            frame.getDateLabel().setText(""+selectedInvoice.getDate());
            frame.getCustomerLabel().setText(""+selectedInvoice.getCustomer());
            frame.getTotalLabel().setText(""+selectedInvoice.getTotal_price());
            LineTableView lineTableView= new LineTableView(selectedInvoice.getLines());
            frame.getLineTable().setModel(lineTableView);
            lineTableView.fireTableDataChanged();
        }
    }


    private void loadFile() {
        JFileChooser fChooser = new JFileChooser();
        try {
            int selection=fChooser.showOpenDialog(null);
            if(selection==JFileChooser.APPROVE_OPTION){
                File headerf=fChooser.getSelectedFile();
                Path headerPath=Paths.get(headerf.getAbsolutePath());
                List<String>headerLines = Files.readAllLines(headerPath);
                System.out.println("file read");
                ArrayList<InvoiceHeader>inv = new ArrayList<>();
                for(String headerline : headerLines){
                    String [] splits = headerline.split(",");
                    int invNum = Integer.parseInt(splits[0]);
                    String invDate = splits[1];
                    String name = splits[2];
                    InvoiceHeader invoice = new InvoiceHeader(invNum,name , invDate);
                    inv.add(invoice);
                }

                selection=fChooser.showOpenDialog(null);
                if(selection==JFileChooser.APPROVE_OPTION){
                    File line = fChooser.getSelectedFile();
                    Path linePath = Paths.get(line.getAbsolutePath());
                    List<String>listLines=Files.readAllLines(linePath);
                    for (String listLine : listLines) {
                        String [] lineSplit = listLine.split(",");
                        int num=Integer.parseInt(lineSplit[0]);
                        String product = lineSplit[1];
                        int price = Integer.parseInt(lineSplit[2]);
                        int count=Integer.parseInt(lineSplit[3]);
                        InvoiceHeader Inv = null;
                        for(InvoiceHeader invoice :inv){
                            if(invoice.getNum()==num){
                                Inv =invoice;
                                break;
                            }
                        }
                        InvoiceLine linesss = new InvoiceLine(num, product, price, count, Inv);
                        Inv.getLines().add(linesss);
                    }

                }

                frame.setInvoicess(inv);
                TableView tableView = new TableView(inv);
                frame.setTableView(tableView);
                frame.getHeaderTable().setModel(tableView);
                frame.getTableView().fireTableDataChanged();
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void InvoiceCreated() {
        String date = invDialog.getDateField().getText();
        String name = invDialog.getCustomerField().getText();
        int invNumber = frame.getMaxNumber();

        InvoiceHeader invoiceHeader = new InvoiceHeader(invNumber, name, date);
        frame.getInvoicess().add(invoiceHeader);
        frame.getTableView().fireTableDataChanged();
        invDialog.setVisible(false);
        invDialog.dispose();
        invDialog = null;
    }

    private void NoInvoiceCreated() {
        invDialog.setVisible(false);
        invDialog.dispose();
        invDialog=null;
    }

    private void LineCreated() {

        String item = lineDialog.getItemField().getText();
        String count = lineDialog.getCountField().getText();
        String price = lineDialog.getPriceField().getText();
        int countUpd = 0;
        double  priceUpd =0;
        try {
            countUpd = Integer.parseInt(count);
            priceUpd = Double.parseDouble(price);
        }
        catch (NumberFormatException xe) {
            System.out.println("Please insert A valid count number or price");
            return;
        }


        int invoiceSelected = frame.getHeaderTable().getSelectedRow();
        if (invoiceSelected != -1){
            InvoiceHeader invoiceHeader= frame.getInvoicess().get(invoiceSelected);
            // InvoiceHeader selectedInvoice = frame.getInvoicess().get(Index);

            InvoiceLine invoiceLine = new InvoiceLine(item, priceUpd, countUpd, invoiceHeader);
            invoiceHeader.getLines().add(invoiceLine);
            LineTableView lineTableView = (LineTableView) frame.getLineTable().getModel();
            frame.getTotalLabel().setText(""+invoiceHeader.getTotal_price());
            lineTableView.fireTableDataChanged();
            frame.getTableView().fireTableDataChanged();
        }

        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null ;
    }

    private void NoLineCreated() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null ;
    }











}
