package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import modelo.Repositorio;
import modelo.User;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.primefaces.event.FlowEvent;

@Named("metadataController")
@SessionScoped
public class MetadataController implements Serializable {

    @EJB
    private negocio.RepositorioFacade ejbFacadeRepositorio;
    private List<Repositorio> itemsRepositorio;
    private List<String> itemsGrupo;
    private List<String> itemsLO;
    private Repositorio selectedRepositorio;
    private String selectedGrupo;
    private String selectedLO;
    private String selectedMetadatos;

    private User user = new User();

    private boolean skip;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void save() {
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + user.getFirstname());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String onFlowProcess(FlowEvent event) {
        if (skip) {
            skip = false;   //reset in case user goes back
            return "confirm";
        } else if (event.getOldStep().equals("repositorio") && event.getNewStep().equals("grupo")) {
            try {
                this.cargarGrupos();
            } catch (JDOMException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "grupo";
        } else if (event.getOldStep().equals("grupo") && event.getNewStep().equals("objeto")) {
            try {
                this.cargarRecords();
            } catch (JDOMException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "objeto";
        }else if (event.getOldStep().equals("objeto") && event.getNewStep().equals("metadatos")) {
            try {
                this.cargarMetadatos();
            } catch (JDOMException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MetadataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "metadatos";
        } else {
            return event.getNewStep();
        }
    }

    private void cargarGrupos() throws JDOMException, IOException {
        String salida = callURL(this.getSelectedRepositorio().getUrlBase() + "?verb=ListSets");
        System.out.println("\nOutput: \n" + salida);

        this.itemsGrupo = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream in = new ByteArrayInputStream(salida.getBytes());
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("archivo.xml"), "utf-8"));
        writer.write(salida);
        writer.close();
        File xmlFile = new File("archivo.xml");

        Document document = (Document) builder.build(xmlFile);
        Element rootNode = document.getRootElement();

        List list = getElememnts(rootNode, "ListSets");
        for (int i = 0; i < list.size(); i++) {
            Element tabla = (Element) list.get(i);
            List lista_campos = getElememnts(tabla, "set");
            for (int j = 0; j < lista_campos.size(); j++) {
                Element campo = (Element) lista_campos.get(j);
                String nombre = getElememnts(campo, "setSpec").get(0).getValue();
                System.out.println("Nombre:\t" + nombre);
                itemsGrupo.add(nombre);
            }

        }
    }

    private void cargarRecords() throws JDOMException, IOException {
        String salida = callURL(this.getSelectedRepositorio().getUrlBase() + "?verb=ListRecords&metadataPrefix=oai_dc&set="+this.selectedGrupo);
        System.out.println("\nOutput: \n" + salida);

        this.itemsLO = new ArrayList<>();
        SAXBuilder builder = new SAXBuilder();
        InputStream in = new ByteArrayInputStream(salida.getBytes());
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("archivo.xml"), "utf-8"));
        writer.write(salida);
        writer.close();
        File xmlFile = new File("archivo.xml");

        Document document = (Document) builder.build(xmlFile);
        Element rootNode = document.getRootElement();

        List list = getElememnts(rootNode, "ListRecords");
        for (int i = 0; i < list.size(); i++) {
            Element tabla = (Element) list.get(i);
            List lista_campos = getElememnts(tabla, "record");
            for (int j = 0; j < lista_campos.size(); j++) {
                Element campo = (Element) lista_campos.get(j);
                List lista_cabecera = getElememnts(campo, "header");
                for (int k = 0; k < lista_cabecera.size(); k++) {
                    Element ident = (Element) lista_cabecera.get(k);
                    String nombre = getElememnts(ident, "identifier").get(0).getValue();
                    System.out.println("Nombre:\t" + nombre);
                    itemsLO.add(nombre);
                }
            }

        }
    }
    
    private void cargarMetadatos() throws JDOMException, IOException {
        String salida = callURL(this.getSelectedRepositorio().getUrlBase() + "?verb=GetRecord&metadataPrefix=oai_dc&identifier="+this.selectedLO);
        System.out.println("\nOutput: \n" + salida);

        SAXBuilder builder = new SAXBuilder();
        InputStream in = new ByteArrayInputStream(salida.getBytes());
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("archivo.xml"), "utf-8"));
        writer.write(salida);
        writer.close();
        File xmlFile = new File("archivo.xml");

        Document document = (Document) builder.build(xmlFile);
        Element rootNode = document.getRootElement();

        List list = getElememnts(rootNode, "GetRecord");
        for (int i = 0; i < list.size(); i++) {
            Element tabla = (Element) list.get(i);
            List lista_campos = getElememnts(tabla, "record");
            for (int j = 0; j < lista_campos.size(); j++) {
                Element campo = (Element) lista_campos.get(j);
                List lista_cabecera = getElememnts(campo, "metadata");
                for (int k = 0; k < lista_cabecera.size(); k++) {
                    Element ident = (Element) lista_cabecera.get(k);
                    for (int g=0;g<ident.getChildren().size();g++){
                        System.out.println("Nombre "+ident.getChildren().get(g).getName());
                        System.out.println("Prefijo "+ident.getChildren().get(g).getNamespacePrefix());
                        System.out.println("URI "+ident.getChildren().get(g).getNamespaceURI());
                    }
                    /////AQUI DEBES TOMAR LOS VALORES DE LOS CAMPOS QUE ESTAN DENTRO DE oai_dc:dc
                    //Fijate en el nombre q tiene el campo puedes ver usando un for como el de arriba si no te coje de una
                    //lo que debes hacer es agregar un for mas para ver cada campo del DC asi como proceso antes los otros campos
                    //
                    String nombre = getElememnts(ident, "dc").get(0).getValue();
                    System.out.println("Nombre:\t" + nombre);
                    this.selectedMetadatos=nombre;
                }
            }

        }
    }

    private static List<Element> getElememnts(Element raiz, String nombre) {
        List<Element> lista = new ArrayList();
        for (int l = 0; l < raiz.getChildren().size(); l++) {
            if (raiz.getChildren().get(l).getName().equals(nombre)) {
                lista.add(raiz.getChildren().get(l));
                System.out.println(raiz.getChildren().get(l));
            }
        }
        return lista;
    }

    public static String callURL(String myURL) {
        System.out.println("Requeted URL:" + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null) {
                urlConn.setReadTimeout(60 * 1000);
            }
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:" + myURL, e);
        }

        return sb.toString();
    }

    /**
     * @return the selectedRepositorio
     */
    public Repositorio getSelectedRepositorio() {
        return selectedRepositorio;
    }

    /**
     * @param selectedRepositorio the selectedRepositorio to set
     */
    public void setSelectedRepositorio(Repositorio selectedRepositorio) {
        this.selectedRepositorio = selectedRepositorio;
    }

    /**
     * @return the itemsRepositorio
     */
    public List<Repositorio> getItemsRepositorio() {
        itemsRepositorio = ejbFacadeRepositorio.findAll();
        return itemsRepositorio;
    }

    /**
     * @param itemsRepositorio the itemsRepositorio to set
     */
    public void setItemsRepositorio(List<Repositorio> itemsRepositorio) {
        this.itemsRepositorio = itemsRepositorio;
    }

    /**
     * @return the itemsGrupo
     */
    public List<String> getItemsGrupo() {
        return itemsGrupo;
    }

    /**
     * @param itemsGrupo the itemsGrupo to set
     */
    public void setItemsGrupo(List<String> itemsGrupo) {
        this.itemsGrupo = itemsGrupo;
    }

    /**
     * @return the selectedGrupo
     */
    public String getSelectedGrupo() {
        return selectedGrupo;
    }

    /**
     * @param selectedGrupo the selectedGrupo to set
     */
    public void setSelectedGrupo(String selectedGrupo) {
        this.selectedGrupo = selectedGrupo;
    }

    /**
     * @return the itemsLO
     */
    public List<String> getItemsLO() {
        return itemsLO;
    }

    /**
     * @param itemsLO the itemsLO to set
     */
    public void setItemsLO(List<String> itemsLO) {
        this.itemsLO = itemsLO;
    }

    /**
     * @return the selectedLO
     */
    public String getSelectedLO() {
        return selectedLO;
    }

    /**
     * @param selectedLO the selectedLO to set
     */
    public void setSelectedLO(String selectedLO) {
        this.selectedLO = selectedLO;
    }

    /**
     * @return the selectedMetadatos
     */
    public String getSelectedMetadatos() {
        return selectedMetadatos;
    }

    /**
     * @param selectedMetadatos the selectedMetadatos to set
     */
    public void setSelectedMetadatos(String selectedMetadatos) {
        this.selectedMetadatos = selectedMetadatos;
    }

}
