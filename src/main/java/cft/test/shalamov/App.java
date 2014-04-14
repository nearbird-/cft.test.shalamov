package cft.test.shalamov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.System;

import cft.test.shalamov.job.Job;
import cft.test.shalamov.job.JobServer;

/**
 * Пример использования JobServer.
 * Слушаем порт, указанный при старте, либо 1337 по умолчанию.
 * Для удобства ручного ввода задач сделано бесконечное чтение соккета.
 * Соккет открывается, например, командой telnet 127.0.0.1 1337.
 * Каждая строка введенного текста осуществляет процесс добавления новой задачи в стек.
 * Для упрощения - вводить только цифры с номером группы.
 * Группы регистрируются при старте приложения и по умолчанию из диапазона 1..pool*2, 
 * где pool - максимальное количество потоков выполнения задач.
 * 
 * Можно открывать несколько соединений.
 * 
 * Исключения выводятся в консоль.
 * 
 * @author Shalamov
 *
 */
public class App {

	enum Arguments {port, pool};
	
	static Integer 	socketServerPort = 1337,
					workerPoolSize = 4;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for (String s : args) {
			String[] attr = s.split("=", 2);
			switch (attr[0]) {
				case "-port":
					socketServerPort = Integer.valueOf(attr[1]);
					break;
				case "-pool":
					workerPoolSize = Integer.valueOf(attr[1]);
					break;
				default:
					System.out.printf("Unknown argument '%s'", attr[0]);
					System.exit(1);
					break;
			}
		}
		
		try {
			//create socket server;
			ServerSocket socketServer = new ServerSocket(socketServerPort);
			//create job server
			JobServer jobServer = new JobServer(workerPoolSize);
			//register tested workers
			for (Integer i = 1; i <= workerPoolSize*2; i++) {
				jobServer.registerWorker(i, new EchoWorker());
			}
			
			Socket clientSocket;
			while (true) {
				try {
					clientSocket = socketServer.accept();

					//free current Thread for new connections 
					new Thread(new Task(jobServer, clientSocket)).start();
				} catch (IOException e) {
					if (!socketServer.isClosed())
						socketServer.close();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (IllegalArgumentException e) {
			System.out.printf("Port %d is outside the specified range of valid port values, which is between 0 and 65535, inclusive", socketServerPort);
			System.exit(1);
		}  
	}
}

class Task implements Runnable {

	private Socket socket;
	private JobServer server;
	
	public Task(JobServer server, Socket socket) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()
			));
			while(true) {
				String group = reader.readLine();
				try {
					server.doJob(new Job(Integer.valueOf(group)));
				} catch (NumberFormatException e) {
					
				} catch (RuntimeException e) {
					System.out.print(e.getMessage());
				}
			}
		} catch (IOException e) {
			
		} finally {
			if (!socket.isClosed()) {
				try { socket.close(); } catch (IOException e) {}
			}
		}
	}
	
}
