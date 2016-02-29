/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Farfan
 */
@Entity
@Table(name = "repositorio")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Repositorio.findAll", query = "SELECT r FROM Repositorio r"),
    @NamedQuery(name = "Repositorio.findByIdRepositorio", query = "SELECT r FROM Repositorio r WHERE r.idRepositorio = :idRepositorio"),
    @NamedQuery(name = "Repositorio.findByNombre", query = "SELECT r FROM Repositorio r WHERE r.nombre = :nombre"),
    @NamedQuery(name = "Repositorio.findByUrlBase", query = "SELECT r FROM Repositorio r WHERE r.urlBase = :urlBase")})
public class Repositorio implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idRepositorio")
    private Integer idRepositorio;
    @Size(max = 45)
    @Column(name = "nombre")
    private String nombre;
    @Size(max = 200)
    @Column(name = "urlBase")
    private String urlBase;

    public Repositorio() {
    }

    public Repositorio(Integer idRepositorio) {
        this.idRepositorio = idRepositorio;
    }

    public Integer getIdRepositorio() {
        return idRepositorio;
    }

    public void setIdRepositorio(Integer idRepositorio) {
        this.idRepositorio = idRepositorio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRepositorio != null ? idRepositorio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Repositorio)) {
            return false;
        }
        Repositorio other = (Repositorio) object;
        if ((this.idRepositorio == null && other.idRepositorio != null) || (this.idRepositorio != null && !this.idRepositorio.equals(other.idRepositorio))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Repositorio[ idRepositorio=" + idRepositorio + " ]";
    }
    
}
