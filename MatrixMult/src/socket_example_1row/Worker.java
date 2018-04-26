package socket_example_1row;

import java.io.*;
import java.net.InetAddress;

import matrix.*;

public class Worker {
	int matrixDimension;
	int nodeNum;
	int localPort;
	Connection conn;
	int dim;
	int[][] a;
	int[][] b;
	int[][] c;
	DataInputStream disCoor;
	DataOutputStream dosCoor;

	DataOutputStream dosLeft;
	DataOutputStream dosUp; // added
	DataInputStream disRight; // added
	DataInputStream disDown; // added

	public Worker(int nodeNum, int localPort) {
		this.nodeNum = nodeNum;
		this.localPort = localPort;
	}

	void configurate(String coorIP, int coorPort) {
		try {
			conn = new Connection(localPort);
			DataIO dio = conn.connectIO(coorIP, coorPort);
			dosCoor = dio.getDos();
			dosCoor.writeInt(nodeNum);
			dosCoor.writeUTF(InetAddress.getLocalHost().getHostAddress());
			dosCoor.writeInt(localPort);
			disCoor = dio.getDis();

			matrixDimension = disCoor.readInt();
			dim = disCoor.readInt(); // get matrix dimension from coordinator
			// width = disCoor.readInt(); dont need this
			a = new int[dim][dim];
			b = new int[dim][dim];
			c = new int[dim][dim];

			String ipLeft = disCoor.readUTF(); // left block connection info
			int portLeft = disCoor.readInt();
			String ipRight = disCoor.readUTF(); // right block connection info
			int portRight = disCoor.readInt();
			String ipUp = disCoor.readUTF(); // up block connection info
			int portUp = disCoor.readInt();
			String ipDown = disCoor.readUTF(); // down block connection info
			int portDown = disCoor.readInt();

			dosLeft = conn.connect2write(ipLeft, portLeft);
			dosUp = conn.connect2write(ipUp, portUp);
			disRight = conn.accept2read();
			disDown = conn.accept2read();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Configuration done.");
	}

	void getMatrixA() {
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				try {
					a[i][j] = disCoor.readInt();
				} catch (IOException ioe) {
					System.out.println("error: " + i + ", " + j);
					ioe.printStackTrace();
				}
			}
		}
	}

	void getMatrixB() {
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				try {
					b[i][j] = disCoor.readInt();
				} catch (IOException ioe) {
					System.out.println("error: " + i + ", " + j);
					ioe.printStackTrace();
				}
			}
		}
	}

	void shiftLeft() {
		// shift matrix a toward left
		for (int i = 0; i < dim; i++) {
			try {
				dosLeft.writeInt(a[i][0]);
			} catch (IOException ioe) {
				System.out.println("error in sending to left, row=" + i);
				ioe.printStackTrace();
			}
		}
		// local shift
		for (int i = 0; i < dim; i++) {
			for (int j = 1; j < dim; j++) {
				a[i][j - 1] = a[i][j];
			}
		}
		// receive the rightmost column
		for (int i = 0; i < dim; i++) {
			try {
				a[i][dim - 1] = disRight.readInt();
			} catch (IOException ioe) {
				System.out.println("error in receiving from right, row=" + i);
				ioe.printStackTrace();
			}
		}
		// send the rotating part to the coordinator
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				try {
					dosCoor.writeInt(a[i][j]);// ** send part
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("LEFT Shifted matrix");
		MatrixMultiple.displayMatrix(a);

	}

	void shiftUp() {
		System.out.println("Up shifted Matrix");
		// shift matrix a toward Up

		for (int i = 0; i < dim; i++) {
			try {
				dosUp.writeInt(b[0][i]);
			} catch (IOException ioe) {
				System.out.println("error in sending to left, row=" + i);
				ioe.printStackTrace();
			}
		}
		// local shift
		for (int i = 0; i < dim; i++) {
			for (int j = 1; j < dim; j++) {
				b[j - 1][i] = b[j][i];
			}
		}

		// receive the upmost column
		for (int i = 0; i < dim; i++) {
			try {
				b[dim - 1][i] = disDown.readInt();
			} catch (IOException ioe) {
				System.out.println("error in receiving from right, row=" + i);
				ioe.printStackTrace();
			}
		}

		MatrixMultiple.displayMatrix(b);

		// send the rotating part to the coordinator
		for (int i = 0; i < b[0].length; i++) {
			for (int j = 0; j < b.length; j++) {
				try {
					dosCoor.writeInt(b[i][j]);// ** send the rotating part to
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("usage: java Worker workerID worker-port-num coordinator-ip coordinator-port-num");
		}
		int workerID = Integer.parseInt(args[0]);
		int portNum = Integer.parseInt(args[1]);
		Worker worker = new Worker(workerID, portNum);
		worker.configurate(args[2], Integer.parseInt(args[3]));

		worker.getMatrixA();
		// MatrixMultiple.displayMatrix(worker.a);

		worker.getMatrixB();
		System.out.println("a");
		MatrixMultiple.displayMatrix(worker.a);

		System.out.println("b");
		MatrixMultiple.displayMatrix(worker.b);

		int n = worker.matrixDimension;

		for (int i = 0; i < n; i++) {
			worker.shiftLeft();
			worker.shiftUp();
		}
		try {
			Thread.sleep(12000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
}
