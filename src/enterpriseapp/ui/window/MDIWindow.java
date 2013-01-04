package enterpriseapp.ui.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.dto.User;
import enterpriseapp.ui.Constants;

/**
 * A Window with MDI (Multiple Document Interface) capabilities.
 * 
 * @author Alejandro Duarte
 *
 */
public class MDIWindow extends Window implements Command, Handler, CloseHandler {
	
	private static final long serialVersionUID = 1L;
	
	protected Action actionNext = new ShortcutAction("", ShortcutAction.KeyCode.SPACEBAR, new int[] {ShortcutAction.ModifierKey.CTRL});
	protected Action actionPrevious = new ShortcutAction("", ShortcutAction.KeyCode.SPACEBAR, new int[] {ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.SHIFT});
	protected Action[] actions = {actionNext, actionPrevious};
	
	protected static int xStart = 35;
	protected static int yStart = 35;
	protected static boolean firstInit = true;
	
	protected List<Module> modules;
	
	protected VerticalLayout workbenchAreaLayout;
	protected VerticalLayout windowLayout;
	protected HorizontalLayout menuLayout;
	protected TabSheet tabsheet;
	protected MenuBar menuBar;
	protected MenuItem layoutMenu;
	protected MenuItem closeAllMenuItem;
	protected MenuItem windowsMenuItem;
	protected MenuItem tabsMenuItem;
	
	protected List<Component> confirmClosingComponents = new ArrayList<Component>(); 
	
	protected int xCurrent = xStart;
	protected int yCurrent = yStart;

	/**
	 * 
	 * @param title Window title.
	 * @param modules modules to add.
	 */
	public MDIWindow(String title, List<Module> modules) {
		super(title);
		this.modules = modules;
	}
	
	/**
	 * Sets up the layout. You must call this to init the window layout.
	 * @param user User accessing the app.
	 * @param addMenusBefore if not null, module menus will be added before the specified menu.
	 */
	public void initWorkbenchContent(User user, String addMenusBefore) {
		windowLayout = new VerticalLayout();
		windowLayout.setSizeFull();
		windowLayout.setStyleName(Reindeer.LAYOUT_BLUE);
		
		addMainMenu();
		initWorkbenchArea();
		addModules(user);
		initWindowMenu(addMenusBefore);
		addActionHandler(this);
		setContent(windowLayout);
	}
	
	protected void addMainMenu() {
		menuBar = new MenuBar();
		menuBar.setWidth("100%");
		
		menuLayout = new HorizontalLayout();
		menuLayout.setWidth("100%");
		menuLayout.addComponent(menuBar);
		menuLayout.setExpandRatio(menuBar, 1);
		
		windowLayout.addComponent(menuLayout);
	}
	
	protected void initWorkbenchArea() {
		tabsheet = new TabSheet();
		tabsheet.setSizeFull();
		tabsheet.setCloseHandler(this);
		
		workbenchAreaLayout = new VerticalLayout();
		workbenchAreaLayout.setSizeFull();
		workbenchAreaLayout.addComponent(tabsheet);
		workbenchAreaLayout.setExpandRatio(tabsheet, 1);
		workbenchAreaLayout.setMargin(true);
		
		windowLayout.addComponent(workbenchAreaLayout);
		windowLayout.setExpandRatio(workbenchAreaLayout, 1);
	}
	
	protected void initWindowMenu(String addMenusBefore) {
		MenuItem addMenusBeforMenuItem = null;
		if(menuBar.getItems() != null) {
			for(MenuItem menuItem : menuBar.getItems()) {
				if(menuItem.getText().equals(addMenusBefore)) {
					addMenusBeforMenuItem = menuItem;
				}
			}
		}
		layoutMenu = menuBar.addItemBefore(Constants.uiTabs, null, null, addMenusBeforMenuItem);
		
		closeAllMenuItem = layoutMenu.addItem(Constants.uiCloseAll, null, this);
		windowsMenuItem = layoutMenu.addItem(Constants.uiWindows, null, this);
		tabsMenuItem = layoutMenu.addItem(Constants.uiTabs, null, this);
		tabsMenuItem.setVisible(false);
	}
	
	protected void addModules(User user) {
		if(modules != null) {
			for(Module module : modules) {
				if(module.userCanAccess(user)) {
					if(firstInit) {
						module.init();
					}
					
					module.add(this, user);
				}
			}
		}
		
		firstInit = false;
	}
	
	/**
	 * Adds the given component as a tab or window acordingly to current visualizacion settings.
	 * @param component component to add.
	 * @param caption Tab or Window caption.
	 * @param icon Tab or Window icon.
	 * @param closable true if the user can close the tab or window.
	 * @param confirmClosing true to show a confirmation dialog before closing the tab or window.
	 */
	public void addWorkbenchContent(Component component, String caption, Resource icon, boolean closable, boolean confirmClosing) {
		component.setSizeFull();
		
		if(windowsMenuItem != null && !windowsMenuItem.isVisible()) {
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setMargin(false);
			layout.addComponent(component);
			
			Window window = new Window(caption);
			window.setIcon(icon);
			window.setClosable(closable);
			window.setContent(layout);
			window.setWidth("80%");
			window.setHeight("80%");
			window.getContent().setSizeFull();
			placeWindow(window);
			
			addWindow(window);
			
		} else {
			Tab tab = tabsheet.addTab(component, caption, icon);
			tab.setClosable(closable);
			tabsheet.setSelectedTab(component);
		}
		
		if(confirmClosing) {
			confirmClosingComponents.add(component);
		}
	}
	
	protected void placeWindow(Window window) {
		window.setPositionX(xCurrent);
		window.setPositionY(yCurrent);
		
		xCurrent += 30;
		yCurrent += 30;
		
		if(xCurrent >= 400) {
			xCurrent = xStart;
		}
		
		if(yCurrent >= 300) {
			yCurrent = yStart;
		}
		
	}
	
	public void closeAllWindows() {
		closeAllWindows(null);
	}
	
	public interface CloseListener {
		boolean close(Component component);
	}
	
	public void closeAllWindows(Class<?> clazz, CloseListener closeListener) {
		if(windowsMenuItem != null && !windowsMenuItem.isVisible()) {
			Set<Window> childWindows = getChildWindows();
			ArrayList<Window> windowsToRemove = new ArrayList<Window>(); 
			
			for(Window window : childWindows) {
				if(window.isClosable() && closeListener.close(window.getContent().getComponentIterator().next())) {
					if(clazz == null || clazz.isAssignableFrom(window.getContent().getComponentIterator().next().getClass())) {
						windowsToRemove.add(window);
					}
				}
			}
			
			for(Window window : windowsToRemove) {
				removeWindow(window);
			}
			
		} else {
			int componentCount = tabsheet.getComponentCount();
			
			ArrayList<Tab> tabsToRemove = new ArrayList<TabSheet.Tab>();
			
			for(int i = 0; i < componentCount; i++) {
				Tab tab = tabsheet.getTab(i);
				
				if(tab.isClosable() && closeListener.close(tab.getComponent())) {
					if(clazz == null || clazz.isAssignableFrom(tab.getComponent().getClass())) {
						tabsToRemove.add(tab);
					}
				}
			}
			
			for(Tab tab : tabsToRemove) {
				tabsheet.removeTab(tab);
			}
		}
	}
	
	public void closeAllWindows(Class<?> clazz) {
		closeAllWindows(clazz, new CloseListener() {
			@Override
			public boolean close(Component component) {
				return true;
			}
		});
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		if(selectedItem.equals(closeAllMenuItem)) {
			closeAllWindows();
		} else if(selectedItem.equals(windowsMenuItem)) {
			viewAsWindows();
		} else if(selectedItem.equals(tabsMenuItem)) {
			viewAsTabs();
		}
	}
	
	/**
	 * 
	 * @return the window main menu bar. You can use this method to add items in your Modules.
	 */
	public MenuBar getMenuBar() {
		return menuBar;
	}

	/**
	 * @param className Module class name to return.
	 * @return The Module with the given class name.
	 */
	public Module getModule(String className) {
		for(Module m : modules) {
			if(m.getClass().getName().equals(className)) {
				return m;
			}
		}
		
		return null;
	}
	
	/**
	 * Toggle to windowed view.
	 */
	public void viewAsWindows() {
		xCurrent = xStart;
		yCurrent = yStart;
		
		tabsheet.setVisible(false);
		layoutMenu.setText(Constants.uiWindows);
		windowsMenuItem.setVisible(false);
		tabsMenuItem.setVisible(true);
		
		while(tabsheet.getComponentCount() != 0) {
			addWorkbenchContent(tabsheet.getTab(0).getComponent(), tabsheet.getTab(0).getCaption(), tabsheet.getTab(0).getIcon(), tabsheet.getTab(0).isClosable(), confirmClosingComponents.contains(tabsheet.getTab(0).getComponent()));
		}
		
		tabsheet.removeAllComponents();
	}
	
	/**
	 * Toggle to tabed view.
	 */
	public void viewAsTabs() {
		layoutMenu.setText(Constants.uiTabs);
		tabsMenuItem.setVisible(false);
		windowsMenuItem.setVisible(true);
		
		for(Window window : getChildWindows()) {
			Component component = window.getComponentIterator().next();
			addWorkbenchContent(component, window.getCaption(), window.getIcon(), window.isClosable(), confirmClosingComponents.contains(component));
		}
		
		while(getChildWindows().size() > 0) {
			removeWindow((Window) getChildWindows().toArray()[0]);
		}
		
		tabsheet.setVisible(true);
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return actions;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if(action.equals(actionNext)) {
			if(!tabsMenuItem.isVisible()) {
				Tab selectedTab = tabsheet.getTab(tabsheet.getSelectedTab());
				int selectedTabPosition = tabsheet.getTabPosition(selectedTab);
				int newTabPosition = 0;
				
				if(selectedTabPosition == tabsheet.getComponentCount() - 1) {
					newTabPosition = 0;
				} else {
					newTabPosition = selectedTabPosition + 1;
				}
				
				tabsheet.setSelectedTab(tabsheet.getTab(newTabPosition).getComponent());
			}
		} else if(action.equals(actionPrevious)) {
			if(!tabsMenuItem.isVisible()) {
				Tab selectedTab = tabsheet.getTab(tabsheet.getSelectedTab());
				int selectedTabPosition = tabsheet.getTabPosition(selectedTab);
				int newTabPosition = 0;
				
				if(selectedTabPosition == 0) {
					newTabPosition = tabsheet.getComponentCount() - 1;
				} else {
					newTabPosition = selectedTabPosition - 1;
				}
				
				tabsheet.setSelectedTab(tabsheet.getTab(newTabPosition).getComponent());
			}
		}
	}

	@Override
	public void onTabClose(final TabSheet tabsheet, final Component tabContent) {
		if(confirmClosingComponents.contains(tabContent)) {
			Utils.yesNoDialog(this, Constants.uiConfirmClose, new ConfirmDialog.Listener() {
				public void onClose(ConfirmDialog dialog) {
					if(dialog.isConfirmed()) {
						tabsheet.removeComponent(tabContent);
						confirmClosingComponents.remove(tabContent);
					}
				}
			});
		} else {
			tabsheet.removeComponent(tabContent);
		}
	}

	public VerticalLayout getWorkbenchAreaLayout() {
		return workbenchAreaLayout;
	}
	
}
