package scripting

class ResourceNotFoundException(resourceName: String, path: String) :
    Exception("Could not find '$resourceName' at '$path'.")
