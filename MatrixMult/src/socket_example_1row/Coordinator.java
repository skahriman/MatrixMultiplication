package socket_example_1row;

import java.io.*;

import matrix.*;

public class Coordinator {

	Connection conn;
	int dim;
	int[][] a;
	int[][] b;
	int[][] c;
	int numNodes;
	DataInputStream[] disWorkers;
	DataOutputStream[] dosWorkers;

	int[][] A;
	int[][] B;
	int[][] RESULT;

	public Coordinator(int n, int numNodes) {
		this.dim = n;
		a = new int[n][n];
		b = new int[n][n];
		c = new int[n][n];
		this.numNodes = numNodes;

		A = new int[n][n];
		B = new int[n][n];
		RESULT = new int[n][n];
	}

	void configurate(int portNum) {
		try {
			conn = new Connection(portNum);
			disWorkers = new DataInputStream[numNodes];
			dosWorkers = new DataOutputStream[numNodes];
			String[] ips = new String[numNodes];
			int[] ports = new int[numNodes];
			for (int i = 0; i < numNodes; i++) {
				DataIO dio = conn.acceptConnect();
				DataInputStream dis = dio.getDis();
				int nodeNum = dis.readInt(); // get worker ID
				ips[nodeNum] = dis.readUTF(); // get worker ip
				ports[nodeNum] = dis.readInt(); // get worker port #
				disWorkers[nodeNum] = dis;
				dosWorkers[nodeNum] = dio.getDos(); // the stream to worker ID

				int length = dim / (int) Math.sqrt(numNodes);

				dosWorkers[nodeNum].writeInt(dim);
				dosWorkers[nodeNum].writeInt(length);

				// int width = (nodeNum<numNodes-1) ? dim/numNodes :
				// dim/numNodes+dim%numNodes;
				// dosWorkers[nodeNum].writeInt(width); //assign matrix width
			}
			int length = (int) Math.sqrt(numNodes);
			for (int i = 0; i < numNodes; i++) {
				int LEFT1, RIGHT1, UP1, DOWN1;
				if (i % (length) == 0) {
					LEFT1 = i - 1 + length;
				} else {
					LEFT1 = i - 1;
				}

				if ((i + 1) % (length) == 0) {
					RIGHT1 = (i + 1 - length);
				} else {
					RIGHT1 = (i + 1);
				}

				UP1 = (i - length + numNodes) % (numNodes);
				DOWN1 = (i + length) % (numNodes);

				dosWorkers[i].writeUTF(ips[LEFT1]);
				dosWorkers[i].writeInt(ports[LEFT1]);
				dosWorkers[i].writeUTF(ips[RIGHT1]);
				dosWorkers[i].writeInt(ports[RIGHT1]);
				dosWorkers[i].writeUTF(ips[UP1]);
				dosWorkers[i].writeInt(ports[UP1]);
				dosWorkers[i].writeUTF(ips[DOWN1]);
				dosWorkers[i].writeInt(ports[DOWN1]);
			}
		} catch (IOException ioe) {
			System.out.println("error: Coordinator assigning neighbor infor.");
			ioe.printStackTrace();
		}
	}

	void distributeLeftShifted(int numNodes) {
		a = MatrixMultiple.createDisplayMatrix(dim); 
		System.out.println("initially created matrix");
		MatrixMultiple.displayMatrix(a); 
		
		a = MatrixMultiple.arrangeMatrixLeft(a);		// arrange matrix by shifting left
		System.out.println("left shifted matrix a");
		MatrixMultiple.displayMatrix(a);
	

		int n = dim / (int) Math.sqrt(numNodes);
		int jnLow = 0;
		int jnHigh = n;
		int inLow = 0;
		int inHigh = n;

		int left = 0;
		int right = (int) Math.sqrt(numNodes);

		int w = 0;
		for (int k = 0; k < (int) Math.sqrt(numNodes); k++) {
			for (int l = left; l < right; l++) {
				for (int i = inLow; i < inHigh; i++) {
					for (int j = jnLow; j < jnHigh; j++) {
						try {
							dosWorkers[w].writeInt(a[i][j]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				w++;
				jnLow = jnHigh;
				jnHigh = jnHigh + n;
			}
			inLow = inHigh;
			inHigh = inHigh + n;
			jnLow = 0;
			jnHigh = n;
		}
	}
	
	void distributeUpShifted(int numNodes) {
		b = MatrixMultiple.createDisplayMatrix(dim);
		
		b = MatrixMultiple.arrangeMatrixUp(b);		// arrange matrix by shifting left
		System.out.println("Up shifted matrix b");
		MatrixMultiple.displayMatrix(b);
	

		int n = dim / (int) Math.sqrt(numNodes);
		int jnLow = 0;
		int jnHigh = n;
		int inLow = 0;
		int inHigh = n;

		int left = 0;
		int right = (int) Math.sqrt(numNodes);

		int w = 0;
		for (int k = 0; k < (int) Math.sqrt(numNodes); k++) {
			for (int l = left; l < right; l++) {
				for (int i = inLow; i < inHigh; i++) {
					for (int j = jnLow; j < jnHigh; j++) {
						try {
							dosWorkers[w].writeInt(b[i][j]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				w++;
				jnLow = jnHigh;
				jnHigh = jnHigh + n;
			}
			inLow = inHigh;
			inHigh = inHigh + n;
			jnLow = 0;
			jnHigh = n;
		}
	}

	void readColumns(int numNodes) {
		int n = dim / (int) Math.sqrt(numNodes);
		int jnLow = 0;
		int jnHigh = n;
		int inLow = 0;
		int inHigh = n;
		int left = 0;
		int right = (int) Math.sqrt(numNodes);

		int w = 0;
		for (int k = 0; k < (int) Math.sqrt(numNodes); k++) {
			for (int l = left; l < right; l++) {
				for (int i = inLow; i < inHigh; i++) {
					for (int j = jnLow; j < jnHigh; j++) {
						try {
							A[i][j] = disWorkers[w].readInt();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				w++;
				jnLow = jnHigh;
				jnHigh = jnHigh + n;
			}
			inLow = inHigh;
			inHigh = inHigh + n;
			jnLow = 0;
			jnHigh = n;
		}

	}

	void readRows(int numNodes) {
		int n = dim / (int) Math.sqrt(numNodes);
		int jnLow = 0;
		int jnHigh = n;
		int inLow = 0;
		int inHigh = n;
		int left = 0;
		int right = (int) Math.sqrt(numNodes);

		int w = 0;
		for (int k = 0; k < (int) Math.sqrt(numNodes); k++) {
			for (int l = left; l < right; l++) {
				for (int i = inLow; i < inHigh; i++) {
					for (int j = jnLow; j < jnHigh; j++) {
						try {
							B[i][j] = disWorkers[w].readInt();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				w++;
				jnLow = jnHigh;
				jnHigh = jnHigh + n;
			}
			inLow = inHigh;
			inHigh = inHigh + n;
			jnLow = 0;
			jnHigh = n;
		}
		System.out.println("--- A ---");
		MatrixMultiple.displayMatrix(this.A, 4);
		System.out.println("--- B ---");
		MatrixMultiple.displayMatrix(B, 4);
		
		calculateResult();
		System.out.println("--- RESULT ---");
		MatrixMultiple.displayMatrix(this.RESULT, 4);
	}

	public int[][] calculateResult() {
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[0].length; j++) {
				RESULT[i][j] = RESULT[i][j] + A[i][j] * B[i][j];
			}
		}
		return RESULT;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("usage: java Coordinator maxtrix-dim number-nodes coordinator-port-num");
		}
		int numNodes = Integer.parseInt(args[1]);
		int n = Integer.parseInt(args[0]);
		Coordinator coor = new Coordinator(n, numNodes);
		coor.configurate(Integer.parseInt(args[2]));
		coor.distributeLeftShifted(numNodes);
		coor.distributeUpShifted(numNodes);

		for (int i = 0; i < n; i++) {
			coor.readColumns(numNodes);
			coor.readRows(numNodes);
		}

		System.out.println("		A		");
		MatrixMultiple.displayMatrix(coor.A);

		System.out.println("		B		");
		MatrixMultiple.displayMatrix(coor.B);
		System.out.println("		RESULT		");
		MatrixMultiple.displayMatrix(coor.RESULT, 5);

		try {
			Thread.sleep(12000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
		
		
		
		int m1[][] = MatrixMultiple.createDisplayMatrix(n);
		int m2[][] = MatrixMultiple.createDisplayMatrix(n);
		int result[][] = MatrixMultiple.multiplyMatrix(m1, m2);
		
		System.out.println("Compare matrices");
		System.out.println(MatrixMultiple.compareMatrix(coor.RESULT, result));
		
	}
}
