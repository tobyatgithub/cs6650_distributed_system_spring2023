package org.distributed;
import java.io.BufferedReader;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response code
    // (and maybe also some value if input is valid)
    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }else if (urlParts.length != 8 && urlParts.length != 3) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url param in 'urlParts'
      if (urlParts.length == 8) {
        int resortID = Integer.parseInt(urlParts[1]);
        int seasonID = Integer.parseInt(urlParts[3]);
        int dayID = Integer.parseInt(urlParts[5]);
        int skierID = Integer.parseInt(urlParts[7]);
      } else {
        int skierId = Integer.parseInt(urlParts[1]);
        String resort = request.getParameter("resort");
        String season = request.getParameter("season");
      }
      response.getWriter().write("It works!");
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skiers, 123]
    try {
      if (urlParts.length == 8) {
        int dayID = Integer.parseInt(urlParts[5]);
        if (!urlParts[2].equalsIgnoreCase("seasons")) {
          return false;
        }
        if (dayID < 1 || dayID > 366) {
          return false;
        }
        if (!urlParts[4].equalsIgnoreCase("day")) {
          return false;
        }
        if (!urlParts[6].equalsIgnoreCase("skier")) {
          System.out.println("skier");
          return false;
        }
        Integer.parseInt(urlParts[1]);
        Integer.parseInt(urlParts[3]);
        Integer.parseInt(urlParts[7]);
      } else if (urlParts.length == 3) {
        Integer.parseInt(urlParts[1]);
      }
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response code
    // (and maybe also some value if input is valid)
    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url param in 'urlParts'
      if (urlParts.length == 8) {
        int resortID = Integer.parseInt(urlParts[1]);
        int seasonID = Integer.parseInt(urlParts[3]);
        int dayID = Integer.parseInt(urlParts[5]);
        int skierID = Integer.parseInt(urlParts[7]);
      } else {
        int skierId = Integer.parseInt(urlParts[1]);
        String resort = request.getParameter("resort");
        String season = request.getParameter("season");
      }
      String body = ReadString(request.getReader());
      response.getWriter().write("It works!");
    }
  }
  private String ReadString(BufferedReader buffer) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = buffer.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }
}
