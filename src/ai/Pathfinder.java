package src.ai;

import java.util.ArrayList;

import src.main.GamePanel;

public class Pathfinder {
    GamePanel gamePanel; 
    public Node[][] node;
    ArrayList<Node> openList = new ArrayList<>();
    Node startNode, goalNode, currentNode;
    public ArrayList<Node> pathList = new ArrayList<>();
    boolean goalReached = false;
    int step = 0;

    public Pathfinder(GamePanel gamePanel){
        this.gamePanel = gamePanel;
        instantiateNode();
    }

    public void instantiateNode(){
        node = new Node[gamePanel.maxWorldCols][gamePanel.maxWorldRows];
        
        int col = 0;
        int row = 0;

        while(col < gamePanel.maxWorldCols && row < gamePanel.maxWorldRows){
            node[col][row] = new Node(col, row);
            col++;
            if(col == gamePanel.maxWorldCols){
                col = 0;
                row ++;
            }
        }
    }

    public void resetNodes(){
        int col = 0;
        int row = 0;

        while(col < gamePanel.maxWorldCols && row < gamePanel.maxWorldRows){
            node[col][row].open = false;
            node[col][row].checked = false;
            node[col][row].solid = false;

            col++;
            if(col == gamePanel.maxWorldCols){
                col = 0;
                row++;
            }
        }

        openList.clear();
        pathList.clear();
        goalReached = false;
        step = 0;
    }

    public void setNodes(int startCol, int startRow, int goalCol, int goalRow){
        resetNodes();

        //Set start and goal nodes
        startNode = node[startCol][startRow];
        currentNode = startNode;
        goalNode = node[goalCol][goalRow];
        openList.add(currentNode);

        int col = 0;
        int row = 0;

        while(col < gamePanel.maxWorldCols && row < gamePanel.maxWorldRows){
            //Set solid node
            int tileNum = gamePanel.tileManager.mapTileNum[gamePanel.currentMap][col][row];
            boolean collision = gamePanel.tileManager.tile[tileNum].collision;

            // —— DEBUG for the goal tile only ——
            if (col == goalCol && row == goalRow) {
                System.out.printf(
                    "DEBUG: goalNode at [%d,%d] → tileNum=%d, collision=%b%n",
                    col, row, tileNum, collision
                );
            }

            node[col][row].solid = collision;

            //Set Cost
            getCost(node[col][row]);

            col++;
            if(col == gamePanel.maxWorldCols){
                col = 0;
                row++;
            }
        }
    }

    public void getCost(Node node){
        // G cost = distance from start
        node.gCost = Math.abs(node.col - startNode.col)
                + Math.abs(node.row - startNode.row);

        // H cost = Manhattan distance to goal
        node.hCost = Math.abs(node.col - goalNode.col)
                + Math.abs(node.row - goalNode.row);

        // F cost = G + H
        node.fCost = node.gCost + node.hCost;
    }

    public boolean search(){
        while(goalReached == false && step < 500){
            int col = currentNode.col;
            int row = currentNode.row;

            //Check current node
            currentNode.checked = true;
            openList.remove(currentNode);

            //Open the up node
            if(row - 1 >= 0){
                openNode(node[col][row - 1]);
            }
            //Open the left node
            if(col - 1 >= 0){
                openNode(node[col - 1][row]);
            }
            //Open the down node
            if(row + 1 < gamePanel.maxWorldRows){
                openNode(node[col][row + 1]);
            }
            //Open the right node
            if(col + 1 < gamePanel.maxWorldCols){
                openNode(node[col + 1][row]);
            }
            //Find the best node
            int bestNodeIndex = 0;
            int bestNodefCost = 999;

            for(int i = 0; i < openList.size(); i++){
                //Check if this node's f cost is better
                if(openList.get(i).fCost < bestNodefCost){
                    bestNodeIndex = i;
                    bestNodefCost = openList.get(i).fCost;
                }
                else if(openList.get(i).fCost == bestNodefCost){
                    if(openList.get(i).gCost < openList.get(bestNodeIndex).gCost){
                        bestNodeIndex = i;
                    }
                }
            }
            //If there is no node in the openList then end loop
            if(openList.size() == 0){
                break;
            }

            //After loop openList[bestNodeIndex] is the next step (= currentNode)
            currentNode = openList.get(bestNodeIndex);

            if(currentNode == goalNode){
                goalReached = true;
                trackThePath();
            }
            step++;
        }

        System.out.println("DEBUG: A* stepsTaken=" + step
        + "   openListRemaining=" + openList.size());
        
        return goalReached;
    }

    public void openNode(Node node){
        if(node.open == false && node.checked == false && node.solid == false){
            node.open = true;
            node.parent = currentNode;
            openList.add(node);
        }
    }

    public void trackThePath(){
        Node current = goalNode;

        while(current != startNode){
            pathList.add(0, current);
            current = current.parent;
        }
    }
}
