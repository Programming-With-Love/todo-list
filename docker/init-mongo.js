db.createUser({
  user: "todo",
  pwd: "123456",
  roles: [
    {
      role: "readWrite",
      db: "todo"
    }
  ]
})