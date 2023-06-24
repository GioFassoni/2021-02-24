package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	
	public Model() {
		this.dao=new PremierLeagueDAO();
		this.grafo=new SimpleDirectedWeightedGraph<Player, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}
	
	public List<Match> getMatches() {
		List<Match> ritorna=this.dao.listAllMatches();
		Collections.sort(ritorna);
		return ritorna;
	}
	
	public void creaGrafo(Match m) {
		Graphs.addAllVertices(grafo, this.dao.getPlayersFromMatch(m.getMatchID()));
		
		for(Player p1:this.grafo.vertexSet()) {
			for(Player p2:this.grafo.vertexSet()) {
				if(p1.getPlayerID()<p2.getPlayerID()) {
					PlayersTeam t1=this.dao.getPlayersTeam(p1.getPlayerID());
					PlayersTeam t2=this.dao.getPlayersTeam(p2.getPlayerID());
					if(t1.getTeamID()!=t2.getTeamID()) {
						double e1=this.getEfficiency(p1, m);
						double e2=this.getEfficiency(p2, m);
						
						if(e1>e2) {
							Graphs.addEdge(grafo, p1, p2, e1-e2);
							this.grafo.setEdgeWeight(p1,  p2, e1-e2);
						}else if(e2>e1) {
							Graphs.addEdge(grafo, p2, p1, e2-e1);
							this.grafo.setEdgeWeight(p2,  p1, e2-e1);
						}
					}
				}
			}
		}
		
	}
	
	public Double getEfficiency(Player p, Match m) {
		double passes = this.dao.getValueForEfficiency(p.getPlayerID(), m.getMatchID()).get(0);
		double assist = this.dao.getValueForEfficiency(p.getPlayerID(), m.getMatchID()).get(1);
		double time = this.dao.getValueForEfficiency(p.getPlayerID(), m.getMatchID()).get(2);
		double eff=(passes+assist)/time;
		return eff;
	}
	
	public Integer numVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public Integer numArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public Double getEfficiencyDifference(Player p) {
		double effOut=0.0;
		double effIn=0.0;
		for(DefaultWeightedEdge e:this.grafo.outgoingEdgesOf(p))
			effOut+=grafo.getEdgeWeight(e);
		for(DefaultWeightedEdge e:this.grafo.incomingEdgesOf(p))
			effIn+=grafo.getEdgeWeight(e);
		
		return effOut-effIn;
	}
	
	public Player getBestPlayer() {
		Double bestScore=0.0;
		Player best=null;
		for(Player p:this.grafo.vertexSet()) {
			if(this.getEfficiencyDifference(p)>bestScore) {
				bestScore=this.getEfficiencyDifference(p);
				best=p;
			}
		}
		return best;
	}
}
