import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TwinderAPI", value = "/TwinderAPI/*")
public class TwinderAPI extends HttpServlet {
    private boolean isUrlValid(String[] urlParts) {
        // TODO: validate the request url path according to the API spec
        // expect: /swipe/{leftorright}/ = [, swipe, left]
        if (urlParts.length != 3) return false;
        if (!urlParts[1].matches("swipe")) return false;
        if (!urlParts[2].matches("left") && !urlParts[2].matches("right")) return false;
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String urlPath = request.getPathInfo();
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
    }
}
