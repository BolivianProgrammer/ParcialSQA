package com.company.inventory.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

@Data
@Entity
@Table(name="product")
public class Product implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7461389651533509262L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private int price;
	
	private int account;
	
	@ManyToOne(fetch= FetchType.LAZY)
	@JsonIgnoreProperties ( {"hibernateLazyInitializer", "handler"})
	private Category category;
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column( name ="picture", columnDefinition = "longblob")
	private byte[] picture;
	
	

}
