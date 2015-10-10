package jav;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;





import javax.swing.JFrame;

import org.gephi.data.attributes.api.*;
import org.gephi.filters.api.*;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.*;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.*;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.*;
import org.gephi.ranking.api.*;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import processing.core.PApplet;

class GephiManager {

	
	public static void run(){
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		//Get models and controllers for this new workspace - will be useful later
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		
		
		//Import file
		Container container;
		try {
			File file = new File("C:/Users/Tassadar/eclipse/CodeBigBro/visualization/data/vis.gexf");
			container = importController.importFile(file);
			container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);   //Force UNDIRECTED
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		//Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);
		
		//See if graph is well imported
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		
		//Filter
		DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
		degreeFilter.init(graph);
		degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));     //Remove nodes with degree < 30
		Query query = filterController.createQuery(degreeFilter);
		GraphView view = filterController.filter(query);
		graphModel.setVisibleView(view);    //Set the filter result as the visible view
		
		//See visible graph stats
		UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
		System.out.println("Nodes: " + graphVisible.getNodeCount());
		System.out.println("Edges: " + graphVisible.getEdgeCount());
		
		//Run YifanHuLayout for 100 passes - The layout always takes the current visible view
		
		//Use force atlas layout
		ForceAtlasLayout layout=new ForceAtlasLayout(null);
		//YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setAdjustSizes(Boolean.TRUE);
		//layout.setOptimalDistance(200f);
		layout.initAlgo();
		
		//run 300 times
		for (int i = 0; i < 1000 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
		
		//Rank color by Degree
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
		rankingController.transform(degreeRanking,colorTransformer);
		
		//Rank size by centrality
//		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
//		Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(0.1f);
		sizeTransformer.setMaxSize(0.2f);

//		rankingController.transform(centralityRanking,sizeTransformer);
		
		//Preview
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
				model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
		model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
		
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.01f));
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_MAX_CHAR, new Integer(5));
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new Font("SimSun", Font.PLAIN, 8));
		
		previewController.refreshPreview();
				  
		//New Processing target, get the PApplet
		ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		PApplet applet = target.getApplet();
		applet.init();
		 
		//Refresh the preview and reset the zoom
		previewController.render(target);
		target.refresh();
		target.resetZoom();
		 
		//Add the applet to a JFrame and display
		JFrame frame = new JFrame("Test Preview");
		frame.setLayout(new BorderLayout());
				 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(applet, BorderLayout.CENTER);
				 
		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args){run();}
}
