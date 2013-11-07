package edu.sjsu.cmpe.procurement.domain;

import org.codehaus.jackson.annotate.JsonProperty;


public class Book {
	@JsonProperty 
	private String category;
	@JsonProperty 
	private String coverimage;
	@JsonProperty
	private long isbn;
	@JsonProperty 
	private String title;
    

    // add more fields here

    /**
     * @return the isbn
     */
    
    public long getIsbn() {
	return isbn;
    }

    /**
     * @param isbn
     *            the isbn to set
     */
    public void setIsbn(long isbn) {
	this.isbn = isbn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCoverimage() {
		return coverimage;
	}

	public void setCoverimage(String coverimage) {
		this.coverimage = coverimage;
	}
}
