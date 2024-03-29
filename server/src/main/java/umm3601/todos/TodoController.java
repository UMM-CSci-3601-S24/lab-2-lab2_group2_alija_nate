package umm3601.todos;

import java.io.IOException;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

/**
 * Controller that manages requests for info about todo.
 */
public class TodoController implements Controller {

  private TodoDatabase todoDatabase;

  /**
   * Construct a controller for todo.
   * <p>
   * This loads the "database" of user info from a JSON file and stores that
   * internally so that (subsets of) todo can be returned in response to
   * requests.
   *
   * @param database the `Database` containing user data
   */
  public TodoController(TodoDatabase todoDatabase) {
    this.todoDatabase = todoDatabase;
  }

  /***
   * Create a database using the json file, use it as data source for a new
   * todoController
   *
   * Constructing the controller might throw an IOException if there are problems
   * reading from the JSON "database" file. If that happens we'll print out an
   * error message exit the program.
   *
   * @throws IOException
   */
  public static TodoController buildTodoController(String todoDataFile) throws IOException {
    TodoController todoController = null;

    TodoDatabase todoDatabase = new TodoDatabase(todoDataFile);
    todoController = new TodoController(todoDatabase);

    return todoController;
  }

  /**
   * Get the single user specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todo user = todoDatabase.getTodo(id);
    if (user != null) {
      ctx.json(user);
      ctx.status(HttpStatus.OK);
    } else {
      throw new NotFoundResponse("No todo with id " + id + " was found.");
    }
  }

  /**
   * Get a JSON response with a list of all the todo in the "database".
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodos(Context ctx) {
    Todo[] todo = todoDatabase.listTodos(ctx.queryParamMap());
    ctx.json(todo);
  }

  /**
   * Setup routes for the `user` collection endpoints.
   *
   * These endpoints are:
   * - `GET /api/todo?age=NUMBER&company=STRING&name=STRING`
   * - List todo, filtered using query parameters
   * - `age`, `company`, and `name` are optional query parameters
   * - `GET /api/todo/:id`
   * - Get the specified user
   *
   * GROUPS SHOULD CREATE THEIR OWN CONTROLLER FOR TODOS THAT
   * IMPLEMENTS THE `Controller` INTERFACE.
   * You'll then implement the `addRoutes` method for that controller,
   * which will set up the routes for that data. The `Server#setupRoutes`
   * method will then call `addRoutes` for each controller, which will
   * add the routes for that controller's data.
   *
   * @param server The Javalin server instance
   */
  @Override
  public void addRoutes(Javalin server) {
    // Get specific user
    server.get("/api/todos/{id}", this::getTodo);

    // List todo, filtered using query parameters
    server.get("/api/todos", this::getTodos);
  }
}
