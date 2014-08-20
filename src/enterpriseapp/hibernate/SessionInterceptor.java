package enterpriseapp.hibernate;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionInterceptor implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionInterceptor.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
		final Session session = Db.getCurrentSession();

		if(session.getTransaction().isActive()) {
			session.getTransaction().commit();
		}

		if(session.isOpen()) {
			session.close();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final Session session = Db.getCurrentSession();

		try {
			if(!session.getTransaction().isActive()) {
				session.beginTransaction();
			}

			chain.doFilter(request, response);

			if(session.getTransaction().isActive()) {
				session.getTransaction().commit();
			}
			
		} catch(StaleObjectStateException e) {
			logger.error("", e);

			if(session.getTransaction().isActive()) {
				session.getTransaction().rollback();
			}

			throw e;
			
		} catch(Throwable e) {
			logger.error("", e);

			if(session.getTransaction().isActive()) {
				session.getTransaction().rollback();
			}

			throw new ServletException(e);
		}
	}
}
