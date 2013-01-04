package enterpriseapp.hibernate.dto;

import java.io.Serializable;

/**
 * Base class for Entity types.
 * @author Alejandro Duarte
 *
 */
public abstract class Dto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getId() == null ? 0 : getId().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Dto other = (Dto) obj;
		if (getId() == null) {
			return false;
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "" + getId();
	}
	
	public abstract Object getId();

	public abstract void setId(Object id);

}
