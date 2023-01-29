import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.Arrays;

@WebServlet(name = "TwinderAPI", value = "/TwinderAPI/*")
public class TwinderAPI extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private boolean isUrlValid(String[] urlParts) {
        // TODO: validate the request url path according to the API spec
        // expect: /swipe/{leftorright}/ = [, swipe, left]
//        System.out.println(urlParts.length);
//        System.out.println(urlParts[0]);
//        System.out.println(urlParts[1]);
//        System.out.println(urlParts[2]);
//        System.out.println(Arrays.toString(urlParts));
        if (urlParts.length != 3) return false;
        if (!urlParts[1].matches("swipe")) return false;
        if (!urlParts[2].matches("left") && !urlParts[2].matches("right")) return false;
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String urlPath = request.getPathInfo();

        System.out.println("Yep2.");
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("empty link");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters!");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            if (urlParts[2].matches("left")) {
                response.getWriter().write("Go left!");
            } else {
                response.getWriter().write("Go right!");
            }
        }

//        Gson gson = new Gson();
//        JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
//
//        // Extract the data from the JSON object
//        String data = json.get("data").getAsString();
//        System.out.println("Data: " + data);
    }
}
