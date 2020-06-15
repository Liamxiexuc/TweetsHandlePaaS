

import java.util.Base64;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;

public class Instance {
	OSClientV3 os = null;
	
	public Instance() {
		os = OSFactory.builderV3()
				.endpoint("https://keystone.rc.nectar.org.au:5000/v3/")
	            .credentials("xiexuc@utas.edu.au", "ODJkNTBhN2MyNjdjYTYx",Identifier.byName("Default"))
	            .scopeToProject(Identifier.byId("7c4ae1df409f46a4ae571f545ffdc746"))
	            .authenticate();
	}
	
//Creating a new instance or VM or Server
	public String createServer() {
		String script = Base64.getEncoder().encodeToString(("#!/bin/bash \n"
				+ "sudo mkdir /home/ubuntu/temp").getBytes());
		
		ServerCreate server = Builders.server()
                .name("Ubuntu 2")
                .flavor("406352b0-2413-4ea6-b219-1a4218fd7d3b")
                .image("45225edb-66d8-4fd0-bf41-132a31a18166")
                .keypairName("kit418tut ")
                .userData(script)
                .build();
		
		return os.compute().servers().boot(server).getId();
	}
	
	public void deleteServer(String serverid) {
		os.compute().servers().delete(serverid); // delete the VM or server
		}
	
	public static void main(String[] args) {
		Instance openstack=new Instance(); 
		String serverid= openstack.createServer();
		System.out.println("Successfully Created Virtual Machine (VM) with¡û-server id"+serverid +" and temp folder inside VM Please log in to¡û-nectar cloud to verify");
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		openstack.deleteServer(serverid);// Delete the created server
		System.out.println("Successfully deleted Virtual Machine (VM) of server id"+serverid);
		
	}
	

}
