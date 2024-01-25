package umm3601.todos;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Main;

/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {

  // An instance of the controller we're testing that is prepared in
  // `setupEach()`, and then exercised in the various tests below.
  private TodoController todoController;
  // An instance of our database "layer" that is prepared in
  // `setupEach()`, and then used in the tests below.
  private static TodoDatabase db;

  // A "fake" version of Javalin's `Context` object that we can
  // use to test with.
  @Mock
  private Context ctx;

  // A captor allows us to make assertions on arguments to method
  // calls that are made "indirectly" by the code we are testing,
  // in this case `json()` calls in `TodoController`. We'll use
  // this to make assertions about the data passed to `json()`.
  @Captor
  private ArgumentCaptor<Todo[]> todoArrayCaptor;

  /**
   * Setup the "database" with some example todos and
   * create a TodoController to exercise in the tests.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @BeforeEach
  public void setUp() throws IOException {
    // Reset our mock context and argument captor
    // (declared above with Mockito annotations @Mock and @Captor)
    MockitoAnnotations.openMocks(this);
    // Construct our "database"
    db = new TodoDatabase(Main.TODO_DATA_FILE);
    // Construct an instance of our controller which
    // we'll then test.
    todoController = new TodoController(db);
  }

  /**
   * Verify that we can successfully build a TodoController
   * and call it's `addRoutes` method. This doesn't verify
   * much beyond that the code actually runs without throwing
   * an exception. We do, however, confirm that the `addRoutes`
   * causes `.get()` to be called at least twice.
   */
  @Test
  public void canBuildController() throws IOException {
    // Call the `TodoController.buildTodoController` method
    // to construct a controller instance "by hand".
    TodoController controller = TodoController.buildTodoController(Main.TODO_DATA_FILE);
    Javalin mockServer = Mockito.mock(Javalin.class);
    controller.addRoutes(mockServer);

    // Verify that calling `addRoutes()` above caused `get()` to be called
    // on the server at least twice. We use `any()` to say we don't care about
    // the arguments that were passed to `.get()`.
    verify(mockServer, Mockito.atLeast(2)).get(any(), any());
  }

  /**
   * Verify that attempting to build a `TodoController` with an
   * invalid `todoDataFile` throws an `IOException`.
   */
  @Test
  public void buildControllerFailsWithIllegalDbFile() {
    Assertions.assertThrows(IOException.class, () -> {
      TodoController.buildTodoController("this is not a legal file name");
    });
  }

  /**
   * Confirm that we can get all the todos.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetAllTodos() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the todos
    // back.
    todoController.getTodos(ctx);

    // Confirm that `json` was called with all the todos.
    // The ArgumentCaptor<Todo[]> todoArrayCaptor was initialized in the @BeforeEach
    // Here, we wait to see what happens *when ctx calls the json method* in the call
    // todoController.getTodos(ctx) and the json method is passed a Todo[]
    // (That's when the Todo[] that was passed as input to the json method is captured)
    verify(ctx).json(todoArrayCaptor.capture());
    // Now that the Todo[] that was passed as input to the json method is captured,
    // we can make assertions about it. In particular, we'll assert that its length
    // is the same as the size of the "database". We could also confirm that the
    // particular todos are the same/correct, but that can get complicated
    // since the order of the todos in the "database" isn't specified. So we'll
    // just check that the counts are correct.
    assertEquals(db.size(), todoArrayCaptor.getValue().length);
  }

  /**
   * Confirm that we can get all the todos with age 25.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosWithCompleteStatus() throws IOException {
    // Add a query param map to the context that maps "age"
    // to "25".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    // Tell the mock `ctx` object to return our query
    // param map when `queryParamMap()` is called.
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // age 25.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have age 25.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals(true, todo.status);
    }
    // Confirm that there are 143 todos with status true
    assertEquals(143, todoArrayCaptor.getValue().length);
  }
  @Test
  public void canGetTodosWithIncompleteStatus() throws IOException {
    // Add a query param map to the context that maps "age"
    // to "25".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    // Tell the mock `ctx` object to return our query
    // param map when `queryParamMap()` is called.
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // age 25.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have age 25.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals(false, todo.status);
    }
    // Confirm that there are 143 todos with status true
    assertEquals(157, todoArrayCaptor.getValue().length);
  }

  /**
   * Confirm that we can get all the todos with company OHMNET.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosWithContains() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {"ipsum"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for OHMNET.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals(true, todo.body.contains("ipsum"));
    }
  }

  @Test
  public void canGetTodosWithContainsNotInList() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {"potato"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for OHMNET.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals(true, todo.body.contains("potato"));
    }
    assertEquals(0, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Barry"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for OHMNET.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals("Barry", todo.owner);
    }
    assertEquals(51, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"homework"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for OHMNET.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals("homework", todo.category);
    }
    assertEquals(79, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosSorted() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"owner"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` work for OHMNET.
    verify(ctx).json(todoArrayCaptor.capture());
    for (int x = 0; x < todoArrayCaptor.getValue().length-1; x++) {
      assertEquals(true, todoArrayCaptor.getValue()[x].owner.compareTo(todoArrayCaptor.getValue()[x+1].owner) <= 0);
    }
  }
  @Test
  public void canGetTodosWithLimit() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {"25"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have ipsum in description
    // and limits it to 3.
    verify(ctx).json(todoArrayCaptor.capture());
    assertEquals(25, todoArrayCaptor.getValue().length);
  }
  /**
   * Test that if the todo sends a request with an illegal value in
   * the age field (i.e., something that can't be parsed to a number)
   * we get a reasonable error code back.
   */
  @Test
  public void respondsAppropriatelyToIllegalLimitInput() {
    // We'll set the requested "age" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {"abc"}));
    // Tell the mock `ctx` object to return our query
    // param map when `queryParamMap()` is called.
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has an age that can't be parsed to a number.
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified limit '" + "abc" + "' can't be parsed to an integer", exception.getMessage());
  }
  /**
   * Confirm that we can get all the todos with age 25 and company OHMNET.
   * This is a "combination" test that tests the interaction of the
   * `age` and `company` query parameters.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosWithGivenBodyandLimit() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {"ipsum"}));
    queryParams.put("limit", Arrays.asList(new String[] {"25"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have ipsum in description
    // and limits it to 3.
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todo todo : todoArrayCaptor.getValue()) {
      assertEquals(true, todo.body.contains("ipsum"));
    }
    assertEquals(25, todoArrayCaptor.getValue().length);
  }


  /**
   * Confirm that we get a todo when using a valid todo ID.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodoWithSpecifiedId() throws IOException {
    // A specific todo ID known to be in the "database".
    String id = "5889598559efd8cf490ef771";
    // Get the todo associated with that ID.
    Todo todo = db.getTodo(id);

    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
  }

  /**
   * Confirm that we get a 404 Not Found response when
   * we request a todo ID that doesn't exist.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(null);
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });
    assertEquals("No todo with id " + null + " was found.", exception.getMessage());
  }
}
