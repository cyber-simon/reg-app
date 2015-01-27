/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.bean.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.StatisticsService;

@ManagedBean
@ViewScoped
public class StatisticsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private StatisticsService service;

	private List<Object> userIdpList;
	private PieChartModel userIdpPie;

	private List<Object> userServiceList;
	private PieChartModel userServicePie;

	private List<Object> userPerMonthList;
	private LineChartModel userPerMonthChart;
	
	private List<Object> registriesPerMonthList;
	private LineChartModel registriesPerMonthChart;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}

	public List<Object> getUserIdpList() {
		if (userIdpList == null) {
			userIdpList = service.countUsersPerIdp();
		}
		return userIdpList;
	}

	public PieChartModel getUserIdpPie() {
		if (userIdpPie == null) {
			userIdpPie = new PieChartModel();
			
			for (Object o : getUserIdpList()) {
				List<Object> ol = (List<Object>) o;
				SamlIdpMetadataEntity i = (SamlIdpMetadataEntity) ol.get(1);
				userIdpPie.set(i.getOrgName(), (Number) ol.get(0));
			}

			userIdpPie.setTitle("User per IDP");
			userIdpPie.setLegendPosition("e");
			userIdpPie.setLegendRows(10);
			userIdpPie.setLegendCols(1);
			userIdpPie.setDiameter(300);
			userIdpPie.setShowDataLabels(true);
		}
		return userIdpPie;
	}

	public List<Object> getUserServiceList() {
		if (userServiceList == null) {
			userServiceList = service.countUsersPerService();
		}
		return userServiceList;
	}

	public PieChartModel getUserServicePie() {
		if (userServicePie == null) {
			userServicePie = new PieChartModel();
			
			for (Object o : getUserServiceList()) {
				List<Object> ol = (List<Object>) o;
				ServiceEntity i = (ServiceEntity) ol.get(1);
				userServicePie.set(i.getName(), (Number) ol.get(0));
			}

			userServicePie.setTitle("User per Service");
			userServicePie.setLegendPosition("e");
			userServicePie.setLegendRows(10);
			userServicePie.setLegendCols(1);
			userServicePie.setDiameter(300);
			userServicePie.setShowDataLabels(true);
		}
		return userServicePie;
	}

	public List<Object> getUserPerMonthList() {
		if (userPerMonthList == null) {
			userPerMonthList = service.countUsersPerMonth();
		}
		return userPerMonthList;
	}

	public LineChartModel getUserPerMonthChart() {
		if (userPerMonthChart == null) {
			userPerMonthChart = new LineChartModel();
			
			ChartSeries cs = new ChartSeries();
			cs.setLabel("Users");
			for (Object o : getUserPerMonthList()) {
				List<Object> ol = (List<Object>) o;
				cs.set((Number) ol.get(1) + "-" + (Number) ol.get(2) + "-01", (Number) ol.get(0));
			}
			userPerMonthChart.addSeries(cs);
			userPerMonthChart.getAxes().put(AxisType.X, new DateAxis());
			userPerMonthChart.getAxis(AxisType.X).setTickAngle(-45);
			userPerMonthChart.getAxis(AxisType.Y).setMin(0);
			userPerMonthChart.setTitle("New Users per Month");
		}
		return userPerMonthChart;
	}

	public List<Object> getRegistriesPerMonthList() {
		if (registriesPerMonthList == null) {
			registriesPerMonthList = service.countRegistriesPerMonthAndService();
		}
		return registriesPerMonthList;
	}

	public LineChartModel getRegistriesPerMonthChart() {
		if (registriesPerMonthChart == null) {
			registriesPerMonthChart = new LineChartModel();

			Map<String, LineChartSeries> seriesMap = new HashMap<String, LineChartSeries>();
			for (Object o : getRegistriesPerMonthList()) {
				List<Object> ol = (List<Object>) o;
				String serviceName = ((ServiceEntity) ol.get(3)).getName();

				if (seriesMap.get(serviceName) == null) {
					LineChartSeries cs = new LineChartSeries();
					cs.setLabel(serviceName);
					//cs.setFill(true);
					seriesMap.put(serviceName, cs);
				}
				seriesMap.get(serviceName).set((Number) ol.get(1) + "-" + (Number) ol.get(2) + "-01", (Number) ol.get(0));
			}
			
			for (LineChartSeries cs : seriesMap.values()) {
				registriesPerMonthChart.addSeries(cs);
			}
			
			registriesPerMonthChart.getAxes().put(AxisType.X, new DateAxis());
			registriesPerMonthChart.setLegendPosition("ne");
			registriesPerMonthChart.getAxis(AxisType.Y).setMin(0);
			registriesPerMonthChart.getAxis(AxisType.X).setTickAngle(-45);
			registriesPerMonthChart.setTitle("Registries per Month");
			registriesPerMonthChart.setZoom(true);
		}
		return registriesPerMonthChart;
	}


}
